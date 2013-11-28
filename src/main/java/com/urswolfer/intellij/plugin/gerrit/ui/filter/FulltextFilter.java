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

package com.urswolfer.intellij.plugin.gerrit.ui.filter;

import com.google.inject.Inject;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ui.SearchFieldAction;
import com.urswolfer.intellij.plugin.gerrit.GerritSettings;
import com.urswolfer.intellij.plugin.gerrit.rest.GerritUtil;
import org.jetbrains.annotations.Nullable;

/**
 * @author Thomas Forrer
 */
public class FulltextFilter extends AbstractChangesFilter {
    @Inject
    private GerritUtil gerritUtil;
    @Inject
    private GerritSettings settings;

    private String value = "";

    @Override
    public AnAction getAction(final Project project) {
        return new SearchFieldAction("Filter: ") {
            @Override
            public void actionPerformed(AnActionEvent event) {
                String newValue = getText().trim();
                if (isNewValue(newValue)) {
                    value = newValue;
                    setChanged();
                    notifyObservers(project);
                }
            }

            private boolean isNewValue(String newValue) {
                return !newValue.equals(value);
            }
        };
    }

    @Override
    @Nullable
    public String getSearchQueryPart() {
        return !value.isEmpty() ? "(" + specialEncodeFulltextQuery(value) + ")" : null;
    }

    /**
     * Queries have some special encoding. {@code URLEncoder.encode(query, "UTF-8")} does not
     * produce correct encoding for the query. It (falsely) encodes brackets, which are expected
     * to remain in the query string as is... This implementation aims to encode only the most
     * commonly used character is the query.
     * @param query a query string to encode
     * @return an encoded version of the passed {@code query}
     */
    private String specialEncodeFulltextQuery(String query) {
        return query
                .replace("+", "%2B")
                .replace(' ', '+')
                .replace("\"", "%22")
                .replace("^", "%5E");
    }
}
