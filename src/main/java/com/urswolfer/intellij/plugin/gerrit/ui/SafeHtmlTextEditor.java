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

package com.urswolfer.intellij.plugin.gerrit.ui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ui.CommitMessage;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.TabbedPaneImpl;
import com.intellij.util.ui.UIUtil;
import com.urswolfer.intellij.plugin.gerrit.util.TextToHtml;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * @author Urs Wolfer
 */
public class SafeHtmlTextEditor extends JPanel {
    private EditorTextField messageField;

    public SafeHtmlTextEditor(Project project) {
        super(new BorderLayout());
        TabbedPaneImpl tabbedPane = new TabbedPaneImpl(SwingConstants.TOP);
        tabbedPane.setKeyboardNavigation(TabbedPaneImpl.DEFAULT_PREV_NEXT_SHORTCUTS);

        messageField = CommitMessage.createCommitTextEditor(project, false);
        messageField.setBorder(BorderFactory.createEmptyBorder());
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(new JLabel("Write your comment here. You can use a simple markdown-like syntax."), BorderLayout.SOUTH);
        tabbedPane.addTab("Write", AllIcons.Actions.Edit, messagePanel);

        final JEditorPane previewEditorPane = new JEditorPane(UIUtil.HTML_MIME, "");
        previewEditorPane.setEditable(false);
        tabbedPane.addTab("Preview", AllIcons.Actions.Preview, previewEditorPane);

        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (((TabbedPaneImpl) e.getSource()).getSelectedComponent() == previewEditorPane) {
                    String content = String.format("<html><head>%s</head><body>%s</body></html>",
                            UIUtil.getCssFontDeclaration(UIUtil.getLabelFont()),
                            TextToHtml.textToHtml(messageField.getText()));
                    previewEditorPane.setText(content);
                }
            }
        });

        add(tabbedPane, SwingConstants.CENTER);
    }

    public EditorTextField getMessageField() {
        return messageField;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 400);
    }
}
