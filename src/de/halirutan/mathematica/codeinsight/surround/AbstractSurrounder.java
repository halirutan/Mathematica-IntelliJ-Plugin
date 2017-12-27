/*
 * Copyright (c) 2017 Patrick Scheibe
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.halirutan.mathematica.codeinsight.surround;

import com.intellij.lang.surroundWith.Surrounder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import de.halirutan.mathematica.lang.psi.api.MathematicaPsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author patrick (6/12/14)
 */
abstract class AbstractSurrounder implements Surrounder {

  @Override
  abstract public String getTemplateDescription();

  @Override
  public boolean isApplicable(@NotNull PsiElement[] elements) {
    return elements.length > 0 && elements[0].getContainingFile() instanceof MathematicaPsiFile;
  }

  protected abstract String getOpening();

  protected abstract String getClosing();

  @Nullable
  @Override
  public TextRange surroundElements(@NotNull Project project, @NotNull Editor editor, @NotNull PsiElement[] elements) throws IncorrectOperationException {
    int selectionStart;
    int selectionEnd;
    final SelectionModel selectionModel = editor.getSelectionModel();

    if (elements.length > 0) {
      selectionStart = elements[0].getTextOffset();
      selectionEnd = elements[elements.length - 1].getTextRange().getEndOffset();
    } else {
      return null;
    }

    final Document document = editor.getDocument();
    if (document.isWritable()) {
      final String expr = document.getText(TextRange.create(selectionStart, selectionEnd));
      document.replaceString(selectionStart, selectionEnd, getOpening() + expr + getClosing());
      modifySelection(TextRange.create(selectionStart, selectionEnd), selectionModel);
    }
    return null;
  }

  void modifySelection(TextRange textRange, SelectionModel model) {
    model.removeSelection();
  }

}
