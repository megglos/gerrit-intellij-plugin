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

import com.google.gerrit.extensions.common.ChangeInfo;
import com.intellij.openapi.actionSystem.DataKey;
import com.urswolfer.intellij.plugin.gerrit.ReviewCommentSink;
import com.urswolfer.intellij.plugin.gerrit.ui.GerritToolWindow;

/**
 * @author Urs Wolfer
 */
public interface GerritDataKeys {
    DataKey<ChangeInfo> CHANGE = DataKey.create("gerrit.Change");
    DataKey<ReviewCommentSink> REVIEW_COMMENT_SINK = DataKey.create("gerrit.ReviewCommentSink");
    DataKey<GerritToolWindow> TOOL_WINDOW = DataKey.create("gerrit.ToolWindow");
}
