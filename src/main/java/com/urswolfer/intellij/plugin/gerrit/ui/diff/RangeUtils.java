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

package com.urswolfer.intellij.plugin.gerrit.ui.diff;

import com.google.common.base.Throwables;
import com.google.gerrit.extensions.common.Comment;
import com.intellij.util.text.CharSequenceReader;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author Urs Wolfer
 */
public class RangeUtils {
    public static Comment.Range textOffsetToRange(CharSequence charsSequence, int start, int end) {
        int startLine = 1;
        int startOffset = -1;
        int endLine = 1;
        int endOffset = -1;
        try {
            BufferedReader reader = new BufferedReader(new CharSequenceReader(charsSequence));
            String lineString;
            int currentCharCount = 0;
            while ((lineString = reader.readLine()) != null) {
                currentCharCount += lineString.length();
                currentCharCount++; // line break

                if (start > currentCharCount) {
                    startLine++;
                } else if (startOffset < 0) {
                    startOffset = start - (currentCharCount - lineString.length() - 1);
                }

                if (end > currentCharCount) {
                    endLine++;
                } else if (endOffset < 0) {
                    endOffset = end - (currentCharCount - lineString.length() - 1);
                    break;
                }
            }
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }

        Comment.Range range = new Comment.Range();
        range.startLine = startLine;
        range.startCharacter = startOffset;
        range.endLine = endLine;
        range.endCharacter = endOffset;
        return range;
    }

    public static Offset rangeToTextOffset(CharSequence charsSequence, Comment.Range range) {
        int startOffset = 0;
        int endOffset = 0;
        try {
            BufferedReader reader = new BufferedReader(new CharSequenceReader(charsSequence));
            String line;
            int textLineCount = 1;
            while ((line = reader.readLine()) != null) {
                if (textLineCount < range.startLine) {
                    startOffset += line.length();
                    startOffset++; // line break
                }
                if (textLineCount < range.endLine) {
                    endOffset += line.length();
                    endOffset++; // line break
                } else {
                    break;
                }
                textLineCount++;
            }
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
        startOffset += range.startCharacter;
        endOffset += range.endCharacter;
        return new Offset(startOffset, endOffset);
    }

    public static class Offset {
        public int start;
        public int end;

        public Offset(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
