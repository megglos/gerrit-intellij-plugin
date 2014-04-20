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

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.gerrit.extensions.api.changes.ReviewInput;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.inject.Inject;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diff.DiffRequest;
import com.intellij.openapi.diff.impl.DiffPanelImpl;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FilePathImpl;
import com.intellij.ui.PopupHandler;
import com.intellij.util.Consumer;
import com.urswolfer.intellij.plugin.gerrit.ReviewCommentSink;
import com.urswolfer.intellij.plugin.gerrit.git.GerritGitUtil;
import com.urswolfer.intellij.plugin.gerrit.rest.GerritUtil;
import com.urswolfer.intellij.plugin.gerrit.util.GerritDataKeys;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author Urs Wolfer
 *
 * Some parts based on code from:
 * https://github.com/ktisha/Crucible4IDEA
 */
public class CommentsDiffTool extends CustomizableFrameDiffTool {
    @Inject
    private GerritGitUtil gerritGitUtil;
    @Inject
    private GerritUtil gerritUtil;
    @Inject
    private DataManager dataManager;
    @Inject
    private AddCommentActionBuilder addCommentActionBuilder;
    @Inject
    private ReviewCommentSink reviewCommentSink;

    private ChangeInfo changeInfo;
    private Project project;

    @Override
    public boolean canShow(DiffRequest request) {
        final boolean superCanShow = super.canShow(request);

        final AsyncResult<DataContext> dataContextFromFocus = dataManager.getDataContextFromFocus();
        final DataContext context = dataContextFromFocus.getResult();
        if (context == null) return false;

        changeInfo = GerritDataKeys.CHANGE.getData(context);
        project = PlatformDataKeys.PROJECT.getData(context);

        return superCanShow && changeInfo != null;
    }

    @Override
    public void diffRequestChange(DiffRequest diffRequest, DiffPanelImpl diffPanel) {
        handleComments(diffPanel, diffRequest.getWindowTitle());
    }

    private void handleComments(final DiffPanelImpl diffPanel, final String filePathString) {
        final FilePath filePath = new FilePathImpl(new File(filePathString), false);

        addCommentAction(diffPanel, filePath, changeInfo);

        gerritUtil.getChangeDetails(changeInfo._number, project, new Consumer<ChangeInfo>() {
            @Override
            public void consume(ChangeInfo changeDetails) {
                gerritUtil.getComments(changeDetails.id, changeDetails.currentRevision, project,
                    new Consumer<Map<String, List<ReviewInput.Comment>>>() {
                        @Override
                        public void consume(Map<String, List<ReviewInput.Comment>> comments) {
                            addCommentsGutter(diffPanel, filePath, comments, changeInfo, project);
                        }
                    });

                String repositoryPath = getGitRepositoryPathForChange(project, changeDetails);
                String relativePath = filePathString.replace(repositoryPath + File.separator, "");
                gerritUtil.getChangeReviewed(changeDetails.id, changeDetails.currentRevision,
                        relativePath, true, project);
            }
        });
    }

    private void addCommentAction(final DiffPanelImpl diffPanel,
                                  @Nullable final FilePath filePath,
                                  ChangeInfo changeInfo) {
            addCommentActionToEditor(diffPanel.getEditor1(), filePath, changeInfo, ReviewInput.Side.PARENT);
            addCommentActionToEditor(diffPanel.getEditor2(), filePath, changeInfo, ReviewInput.Side.REVISION);
    }

    private void addCommentActionToEditor(@Nullable Editor editor,
                                          @Nullable FilePath filePath,
                                          ChangeInfo changeInfo,
                                          ReviewInput.Side commentSide) {
        if (editor != null) {
            DefaultActionGroup group = new DefaultActionGroup();
            final AddCommentAction addCommentAction = addCommentActionBuilder.build(
                    reviewCommentSink,
                    changeInfo,
                    editor,
                    filePath,
                    commentSide);
            addCommentAction.setContextComponent(editor.getComponent());
            group.add(addCommentAction);
            PopupHandler.installUnknownPopupHandler(editor.getContentComponent(), group, ActionManager.getInstance());
        }
    }

    private void addCommentsGutter(DiffPanelImpl diffPanel,
                                   FilePath filePath,
                                   Map<String, List<ReviewInput.Comment>> comments,
                                   ChangeInfo changeInfo,
                                   Project project) {
        String repositoryPath = getGitRepositoryPathForChange(project, changeInfo);

        List<ReviewInput.Comment> fileComments = Lists.newArrayList();
        for (Map.Entry<String, List<ReviewInput.Comment>> entry : comments.entrySet()) {
            if (isForCurrentFile(filePath, entry.getKey(), repositoryPath)) {
                fileComments = entry.getValue();
                break;
            }
        }

        Iterable<ReviewInput.Comment> commentInputsFromSink = reviewCommentSink.getCommentsForChange(changeInfo.id);
        for (ReviewInput.Comment commentInput : commentInputsFromSink) {
            if (isForCurrentFile(filePath, commentInput.path, repositoryPath)) {
                fileComments.add(commentInput);
            }
        }

        for (ReviewInput.Comment fileComment : fileComments) {
            MarkupModel markup;
            if (fileComment.side != null && fileComment.side.equals(ReviewInput.Side.PARENT)) {
                markup = diffPanel.getEditor1().getMarkupModel();
            } else {
                markup = diffPanel.getEditor2().getMarkupModel();
            }
            int lineCount = markup.getDocument().getLineCount();

            int line = fileComment.line - 1;
            if (line < 0) {
                line = 0;
            }
            if (line > lineCount - 1) {
                line = lineCount - 1;
            }
            final RangeHighlighter highlighter = markup.addLineHighlighter(line, HighlighterLayer.ERROR + 1, null);
            highlighter.setGutterIconRenderer(new CommentGutterIconRenderer(fileComment, reviewCommentSink, changeInfo, highlighter, markup));
        }
    }

    private boolean isForCurrentFile(FilePath currentFilePath, String projectFilePath, String repositoryPath) {
        return currentFilePath.getPath().equals(repositoryPath + File.separator + projectFilePath);
    }

    private String getGitRepositoryPathForChange(Project project, ChangeInfo changeInfo) {
        Optional<GitRepository> gitRepositoryOptional = gerritGitUtil.getRepositoryForGerritProject(project, changeInfo.project);
        if (!gitRepositoryOptional.isPresent()) return null;
        GitRepository repository = gitRepositoryOptional.get();
        String repositoryPath = repository.getRoot().getPath();
        return repositoryPath;
    }
}
