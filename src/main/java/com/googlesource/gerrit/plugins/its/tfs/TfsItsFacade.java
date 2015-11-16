// Copyright (C) 2015 Björn Rennfanz <bjoern@fam-rennfanz.de>
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlesource.gerrit.plugins.its.tfs;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;

import com.microsoft.tfs.core.clients.workitem.CoreFieldReferenceNames;
import com.microsoft.tfs.core.clients.workitem.WorkItem;
import com.microsoft.tfs.core.clients.workitem.WorkItemClient;
import com.microsoft.tfs.core.clients.workitem.WorkItemServerVersion;
import com.microsoft.tfs.core.clients.workitem.fields.AllowedValuesCollection;
import com.microsoft.tfs.core.exceptions.TECoreException;
import com.microsoft.tfs.core.httpclient.Credentials;
import com.microsoft.tfs.core.httpclient.UsernamePasswordCredentials;
import com.microsoft.tfs.core.util.URIUtils;
import com.microsoft.tfs.core.TFSTeamProjectCollection;

import org.eclipse.jgit.lib.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.its.base.its.ItsFacade;
import com.googlesource.gerrit.plugins.its.base.its.InvalidTransitionException;

public class TfsItsFacade implements ItsFacade
{
  // Private static members
  private static final String GERRIT_CONFIG_USERNAME = "username";
  private static final String GERRIT_CONFIG_PASSWORD = "password";
  private static final String GERRIT_CONFIG_URL = "url";

  private static final int MAX_ATTEMPTS = 3;

  // Private members
  private Logger log = LoggerFactory.getLogger(TfsItsFacade.class);
  private String pluginName;
  private Config gerritConfig;

  private TFSTeamProjectCollection teamProjectCollection;
  private WorkItemClient workItemClient;
  private Credentials credentials;

  @Inject
  public TfsItsFacade(@PluginName String pluginName,
      @GerritServerConfig Config cfg)
  {
    this.pluginName = pluginName;

    // Try to connect Team Foundation Server
    this.credentials = new UsernamePasswordCredentials(getUsername(), getPassword());
    this.teamProjectCollection = new TFSTeamProjectCollection(URIUtils.newURI(getUrl()), credentials);
    this.workItemClient = teamProjectCollection.getWorkItemClient();

    // Check if we are connected successfully to server
    if (teamProjectCollection.hasAuthenticated())
    {
      // Log team foundation server informations
      final WorkItemServerVersion serverVersion = workItemClient.getVersion();
      log.info("Connected to Team Foundation Server at " + getUrl() + ", reported version is " + serverVersion.toString());
    }
    else
    {
      // Warn about no connection to server
      log.warn("Team Foundation Server is currently not available");
    }
  }

  @Override
  public String healthCheck(final Check check) throws IOException
  {
    return execute(new Callable<String>()
    {
      @Override
      public String call() throws Exception
      {
        if (check.equals(Check.ACCESS))
        {
          return ""; //healthCheckAccess();
        }
        else
        {
          return ""; //healthCheckSysinfo();
        }
      }
    });
  }

  @Override
  public void addRelatedLink(final String issueId, final URL relatedUrl, final String description) throws IOException
  {
    // Add comment with URL
    addComment(issueId, "Related URL: " + createLinkForWebui(relatedUrl.toExternalForm(), description));
  }

  @Override
  public void addComment(final String issueId, final String comment) throws IOException
  {
    execute(new Callable<String>()
    {
      @Override
      public String call() throws Exception
      {
        WorkItem workItem = workItemClient.getWorkItemByID(convertIssueId(issueId));
        workItem.getFields().getField(CoreFieldReferenceNames.HISTORY).setValue(comment);

        return issueId;
      }
    });
  }

  @Override
  public void performAction(final String issueId, final String actionName) throws IOException
  {
    execute(new Callable<String>()
    {
      @Override
      public String call() throws Exception
      {
        doPerformAction(issueId, actionName);
        return issueId;
      }
    });
  }

  private void doPerformAction(final String issueId, final String actionName) throws IOException
  {
    WorkItem workItem = workItemClient.getWorkItemByID(convertIssueId(issueId));
    AllowedValuesCollection actions = workItem.getFields().getField(CoreFieldReferenceNames.STATE).getAllowedValues();

    // Check if action is allowed
    if (actions.contains(actionName))
    {
      log.debug("Executing action " + actionName + " on issue " + issueId);
      workItem.getFields().getField(CoreFieldReferenceNames.STATE).setValue(actionName);
    }
    else
    {
      StringBuilder sb = new StringBuilder();

      for (String action : actions.getValues())
      {
        if (sb.length() > 0) sb.append(',');
        sb.append('\'');
        sb.append(action);
        sb.append('\'');
      }

      log.error("Action " + actionName + " not found within available actions: " + sb);
      throw new InvalidTransitionException("Action " + actionName + " not executable on issue " + issueId);
    }
  }

  @Override
  public boolean exists(final String issueId) throws IOException
  {
    return execute(new Callable<Boolean>()
    {
      @Override
      public Boolean call() throws Exception
      {
        final int issueIdInt = convertIssueId(issueId);
        return workItemClient.getWorkItemByID(issueIdInt) != null;
      }
    });
  }

  @Override
  public String createLinkForWebui(String url, String text)
  {
    return "[" + text + "|" + url + "]";
  }

  private <P> P execute(Callable<P> function) throws IOException
  {
    int attempt = 0;
    while (true)
    {
      try
      {
        return function.call();
      }
      catch (Exception ex)
      {
        if ((ex instanceof TECoreException) && (++attempt < MAX_ATTEMPTS))
        {
          log.debug("Call failed - retrying, attempt {} of {}", attempt, MAX_ATTEMPTS);

          // Close connection
          this.teamProjectCollection.close();

          // Try to connect Team Foundation Server again
          this.credentials = new UsernamePasswordCredentials(getUsername(), getPassword());
          this.teamProjectCollection = new TFSTeamProjectCollection(URIUtils.newURI(getUrl()), credentials);
          this.workItemClient = teamProjectCollection.getWorkItemClient();

          // Try call again
          continue;
        }

        if (ex instanceof IOException)
        {
          throw ((IOException)ex);
        }
        else
        {
          throw new IOException(ex);
        }
      }
    }
  }

  private String getPassword()
  {
    final String pass = gerritConfig.getString(pluginName, null, GERRIT_CONFIG_PASSWORD);
    return pass;
  }

  private String getUsername()
  {
    final String user = gerritConfig.getString(pluginName, null, GERRIT_CONFIG_USERNAME);
    return user;
  }

  private String getUrl()
  {
    final String url = gerritConfig.getString(pluginName, null, GERRIT_CONFIG_URL);
    return url;
  }

  private int convertIssueId(String issueId)
  {
    String issueIdDigits = issueId.replaceAll("\\D+", "");
    return Integer.parseInt(issueIdDigits);
  }
}
