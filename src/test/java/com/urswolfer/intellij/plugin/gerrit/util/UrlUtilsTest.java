/*
 * Copyright 2013 Urs Wolfer
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

package com.urswolfer.intellij.plugin.gerrit.util;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URI;

/**
 * @author Urs Wolfer
 */
public class UrlUtilsTest {

    @Test
    public void testUrlHasSameHost() throws Exception {
        Assert.assertTrue(UrlUtils.urlHasSameHost("https://gerrit.example.com/test.git", "https://gerrit.example.com/"));

        Assert.assertFalse(UrlUtils.urlHasSameHost("https://git.example.com/test.git", "https://gerrit.example.com/"));
    }

    @Test
    public void testCreateUriFromGitConfigString() throws Exception {
        URI uriFromGitConfigString = UrlUtils.createUriFromGitConfigString("https://git.example.com/");
        Assert.assertEquals(uriFromGitConfigString.toString(), "https://git.example.com/");

        URI uriFromGitConfigStringWithoutProtocol = UrlUtils.createUriFromGitConfigString("git.example.com/");
        Assert.assertEquals(uriFromGitConfigStringWithoutProtocol.toString(), "git://git.example.com/");
    }
}
