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

package com.urswolfer.intellij.plugin.gerrit.ui.action;

import com.google.common.base.Optional;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.inject.Inject;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.ui.table.TableView;
import com.intellij.util.Consumer;
import com.urswolfer.intellij.plugin.gerrit.rest.GerritUtil;

import javax.swing.*;
import java.awt.*;

/**
 * @author Urs Wolfer
 */
public abstract class AbstractChangeAction extends AnAction implements DumbAware {
    @Inject
    protected GerritUtil gerritUtil;

    public AbstractChangeAction(String text, String description, Icon icon) {
        super(text, description, icon);
    }

    protected Optional<ChangeInfo> getSelectedChange(AnActionEvent anActionEvent) {
        Component component = anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT);
        if (!(component instanceof TableView)) {
            return Optional.absent();
        }
        final TableView table = (TableView) component;
        Object selectedObject = table.getSelectedObject();
        if (!(selectedObject instanceof ChangeInfo)) {
            return Optional.absent();
        }
        final ChangeInfo selectedChange = (ChangeInfo) selectedObject;
        return Optional.fromNullable(selectedChange);
    }

    protected void getChangeDetail(ChangeInfo selectedChange, Project project, final Consumer<ChangeInfo> consumer) {
        gerritUtil.getChangeDetails(selectedChange._number, project, consumer);
    }
}
