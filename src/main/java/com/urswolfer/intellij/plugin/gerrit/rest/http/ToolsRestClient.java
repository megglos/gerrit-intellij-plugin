/*
 * Copyright 2013-2014 Urs Wolfer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.urswolfer.intellij.plugin.gerrit.rest.http;

import com.urswolfer.intellij.plugin.gerrit.rest.GerritClientException;
import com.urswolfer.intellij.plugin.gerrit.rest.ToolsClient;
import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

/**
 * @author Urs Wolfer
 */
public class ToolsRestClient extends AbstractRestClient implements ToolsClient {
    private final GerritRestClient gerritRestClient;

    public ToolsRestClient(GerritRestClient gerritRestClient) {
        this.gerritRestClient = gerritRestClient;
    }

    @Override
    public InputStream getCommitMessageHook() throws GerritClientException {
        try {
            HttpResponse response = gerritRestClient.doRest("/tools/hooks/commit-msg", null, Collections.<Header>emptyList(), GerritRestClient.HttpVerb.GET);
            return response.getEntity().getContent();
        } catch (IOException e) {
            throw new GerritClientException(e);
        }
    }
}
