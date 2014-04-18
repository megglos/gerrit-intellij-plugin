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

package com.urswolfer.gerrit.client.rest.bean;

/**
 * @author Urs Wolfer
 */
public class CommentInput extends CommentBase {

    public CommentInfo toCommentInfo() {
        CommentInfo commentInfo = new CommentInfo();
        commentInfo.setMessage(getMessage());
        commentInfo.setLine(getLine());
        commentInfo.setPath(getPath());
        commentInfo.setSide(getSide());
        AccountInfo author = new AccountInfo();
        author.setName("Myself");
        commentInfo.setAuthor(author);
        return commentInfo;
    }
}
