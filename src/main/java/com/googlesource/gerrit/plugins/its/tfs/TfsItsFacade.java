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

import com.microsoft.tfs.core.clients.workitem.WorkItemServerVersion;
import com.microsoft.tfs.core.httpclient.Credentials;
import com.microsoft.tfs.core.httpclient.UsernamePasswordCredentials;
import com.microsoft.tfs.core.util.URIUtils;
import org.eclipse.jgit.lib.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.its.base.its.ItsFacade;

import com.microsoft.tfs.core.TFSTeamProjectCollection;

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
  private Credentials credentials;

  @Inject
  public TfsItsFacade(@PluginName String pluginName,
      @GerritServerConfig Config cfg)
  {
    this.pluginName = pluginName;

    // Try to connect Team Foundation Server
    this.credentials = new UsernamePasswordCredentials(getUsername(), getPassword());
    this.teamProjectCollection = new TFSTeamProjectCollection(URIUtils.newURI(getUrl()), credentials);
    WorkItemServerVersion version = teamProjectCollection.getWorkItemClient().getVersion();

    // Check if we are connected successfully to server
    if (teamProjectCollection.hasAuthenticated())
    {
      // Log team foundation server informations
      log.info("Connected to Team Foundation Server at " + getUrl() + ", reported version is " + version.toString());
    }
    else
    {
      // Warn about no connection to server
      log.warn("Team Foundation Server is currently not available");
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

  @Override
  public String healthCheck(Check check) throws IOException
  {
    return null;
  }

  @Override
  public void addRelatedLink(String issueId, URL relatedUrl, String description) throws IOException
  {

  }

  @Override
  public void addComment(String issueId, String comment) throws IOException
  {

  }

  @Override
  public void performAction(String issueId, String actionName) throws IOException
  {

  }

  @Override
  public boolean exists(String issueId) throws IOException
  {
    return false;
  }

  @Override
  public String createLinkForWebui(String url, String text)
  {
    return null;
  }
}
