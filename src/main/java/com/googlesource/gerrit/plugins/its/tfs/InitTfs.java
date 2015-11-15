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

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.pgm.init.api.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.googlesource.gerrit.plugins.its.base.its.InitIts;
import com.googlesource.gerrit.plugins.its.base.validation.ItsAssociationPolicy;
import com.microsoft.tfs.core.TFSTeamProjectCollection;
import com.microsoft.tfs.core.httpclient.Credentials;
import com.microsoft.tfs.core.httpclient.UsernamePasswordCredentials;
import com.microsoft.tfs.core.util.URIUtils;
import org.eclipse.jgit.errors.ConfigInvalidException;

import java.io.IOException;

/** Initialize the Team Foundation Server ITS gerrit plugin. */
@Singleton
class InitTfs extends InitIts
{
  private static final String COMMENT_REGEX_SECTION = "commentRegex";
  private final String pluginName;
  private final Section.Factory sections;
  private final InitFlags flags;

  private Section tfs;
  private Section tfsComment;

  private String tfsUrl;
  private String tfsUsername;
  private String tfsPassword;

  @Inject
  InitTfs(@PluginName String pluginName, ConsoleUI ui,
    Section.Factory sections, AllProjectsConfig allProjectsConfig,
    AllProjectsNameOnInitProvider allProjects, InitFlags flags)
  {
    super(pluginName, "TFS", ui, allProjectsConfig, allProjects);
    this.pluginName = pluginName;
    this.sections = sections;
    this.flags = flags;
  }

  @Override
  public void run() throws IOException, ConfigInvalidException
  {
    super.run();

    ui.message("\n");
    ui.header("Team Foundation Server connectivity");

    // Initialize Its-TFS plugin
    init();
  }

  private void init() {
    this.tfs = sections.get(pluginName, null);
    this.tfsComment = sections.get(COMMENT_LINK_SECTION, pluginName);

    do
    {
      // Ask user for credentials and connection url
      enterTfsConnectivity();
    }
    while (tfsUrl != null && (isConnectivityRequested(tfsUrl) && !isTfsConnectSuccessful()));

    // Check if connection was not successfully
    if (tfsUrl == null)
    {
      return;
    }

    ui.header("Team Foundation Server issue-tracking association");
    tfsComment.string("Team Foundation Server issue-Id regex", "match", "([A-Z]+-[0-9]+)");
    tfsComment.select("Issue-id enforced in commit message", "association", ItsAssociationPolicy.SUGGESTED);
  }

  public void enterTfsConnectivity()
  {
    tfsUrl = tfs.string("Team Foundation Server URL (empty to skip)", "url", null);
    if (tfsUrl != null)
    {
      tfsUsername = tfs.string("Team Foundation Server username", "username", "");
      tfsPassword = tfs.password("username", "password");
    }
  }

  private boolean isTfsConnectSuccessful()
  {
    boolean result;
    ui.message("Checking Team Foundation Server connectivity ... ");

    // Try to connect Team Foundation Server with credentials from config
    Credentials credentials = new UsernamePasswordCredentials(tfsUsername, tfsPassword);
    TFSTeamProjectCollection tpc = new TFSTeamProjectCollection(URIUtils.newURI(tfsUrl), credentials);

    // Check if connection was successfully
    if (tpc.hasAuthenticated())
    {
      ui.message("[OK]\n");
      result = true;
    }
    else
    {
      ui.message("*FAILED* Unable to authenticate\n");
      result = false;
    }

    // Close connection
    tpc.close();

    return result;
  }
}
