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

package com.urswolfer.intellij.plugin.gerrit.ui.diff;

import com.google.gerrit.extensions.api.changes.DraftInput;
import com.google.gerrit.extensions.common.Comment;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.EditorTextField;
import com.urswolfer.intellij.plugin.gerrit.ui.SafeHtmlTextEditor;
import com.urswolfer.intellij.plugin.gerrit.util.PathUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * @author Urs Wolfer
 *
 * Some parts based on code from:
 * https://github.com/ktisha/Crucible4IDEA
 */
public class CommentForm extends JPanel {
    private static final int BALLOON_WIDTH = 550;
    private static final int BALLOON_HEIGHT = 300;

    private final Editor editor;
    private final String filePath;
    private final Comment.Side commentSide;
    private final Comment commentToEdit;

    private final EditorTextField reviewTextField;
    private JBPopup balloon;
    private DraftInput commentInput;

    public CommentForm(Project project,
                       Editor editor,
                       String filePath,
                       Comment.Side commentSide,
                       Comment commentToEdit) {
        super(new BorderLayout());

        this.filePath = filePath;
        this.editor = editor;
        this.commentSide = commentSide;
        this.commentToEdit = commentToEdit;

        SafeHtmlTextEditor safeHtmlTextEditor = new SafeHtmlTextEditor(project);
        reviewTextField = safeHtmlTextEditor.getMessageField();
        add(safeHtmlTextEditor);

        reviewTextField.setPreferredSize(new Dimension(BALLOON_WIDTH, BALLOON_HEIGHT));

        reviewTextField.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), "postComment");
        reviewTextField.getActionMap().put("postComment", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commentInput = createComment();
                balloon.dispose();
            }
        });

        if (commentToEdit != null) {
            reviewTextField.setText(commentToEdit.message);
        }
    }

    private DraftInput createComment() {
        DraftInput comment = new DraftInput();

        comment.message = getText();
        comment.path = PathUtils.ensureSlashSeparators(filePath);
        comment.side = commentSide;

        SelectionModel selectionModel = editor.getSelectionModel();
        if (selectionModel.hasSelection()) {
            comment.range = handleRangeComment(selectionModel);
            comment.line = comment.range.endLine; // end line as per specification
        } else {
            comment.line = editor.getDocument().getLineNumber(editor.getCaretModel().getOffset()) + 1;
        }

        if (commentToEdit != null) { // preserve: the selection might not exist anymore but we should not loose it
            comment.range = commentToEdit.range;
            comment.line = commentToEdit.line;
            comment.inReplyTo = commentToEdit.inReplyTo;
        }

        return comment;
    }

    public void requestFocus() {
        IdeFocusManager.findInstanceByComponent(reviewTextField).requestFocus(reviewTextField, true);
    }

    @NotNull
    public String getText() {
        return reviewTextField.getText();
    }

    public void setBalloon(@NotNull final JBPopup balloon) {
        this.balloon = balloon;
    }

    public DraftInput getComment() {
        return commentInput;
    }

    private Comment.Range handleRangeComment(SelectionModel selectionModel) {
        int startSelection = selectionModel.getBlockSelectionStarts()[0];
        int endSelection = selectionModel.getBlockSelectionEnds()[0];
        CharSequence charsSequence = editor.getMarkupModel().getDocument().getCharsSequence();
        return RangeUtils.textOffsetToRange(charsSequence, startSelection, endSelection);
    }
}
