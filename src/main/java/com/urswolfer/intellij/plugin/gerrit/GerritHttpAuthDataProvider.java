/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
package com.urswolfer.intellij.plugin.gerrit;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.AuthData;
import com.urswolfer.intellij.plugin.gerrit.rest.GerritApiUtil;
import git4idea.jgit.GitHttpAuthDataProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Parts based on org.jetbrains.plugins.github.extensions.GithubHttpAuthDataProvider
 *
 * @author Urs Wolfer
 * @author Kirill Likhodedov
 */
public class GerritHttpAuthDataProvider implements GitHttpAuthDataProvider {

    @Nullable
    @Override
    public AuthData getAuthData(@NotNull String url) {
        GerritSettings settings = GerritSettings.getInstance();

        if (!GerritApiUtil.getApiUrl().equalsIgnoreCase(url)) {
            return null;
        }
        if (StringUtil.isEmptyOrSpaces(settings.getLogin()) || StringUtil.isEmptyOrSpaces(settings.getPassword())) {
            return null;
        }
        return new AuthData(settings.getLogin(), settings.getPassword());
    }
}
