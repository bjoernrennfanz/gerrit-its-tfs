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
import com.google.gerrit.server.config.GerritServerConfig;

import com.googlesource.gerrit.plugins.its.base.its.ItsFacade;
import org.eclipse.jgit.lib.Config;

import java.io.IOException;
import java.net.URL;

public class TfsItsFacade implements ItsFacade
{
  private static final String GERRIT_CONFIG_USERNAME = "username";
  private static final String GERRIT_CONFIG_PASSWORD = "password";
  private static final String GERRIT_CONFIG_URL = "url";

  public TfsItsFacade(@PluginName String pluginName,
      @GerritServerConfig Config cfg)
  {

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
