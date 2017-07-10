/*
 * Copyright (c) 2015 Patrick Scheibe
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

package de.halirutan.mathematica.lang.psi.impl.slots;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import de.halirutan.mathematica.lang.psi.MathematicaVisitor;
import de.halirutan.mathematica.lang.psi.api.slots.SlotExpression;
import de.halirutan.mathematica.lang.psi.impl.ExpressionImpl;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of Association style slot expressions: #key, #"key", #["key"] and #[key]
 * Created by rsmenon on 11/8/15.
 */
public class SlotExpressionImpl extends ExpressionImpl implements SlotExpression {
  public SlotExpressionImpl(@NotNull final ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MathematicaVisitor) {
      ((MathematicaVisitor) visitor).visitSlotExpression(this);
    } else {
      super.accept(visitor);
    }
  }

  @Override
  public String toString() {
    return "AssociationSlot[" + getText() + "]";
  }
}
