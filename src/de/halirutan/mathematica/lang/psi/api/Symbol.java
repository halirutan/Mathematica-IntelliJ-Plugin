/*
 * Copyright (c) 2018 Patrick Scheibe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.halirutan.mathematica.lang.psi.api;

import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import de.halirutan.mathematica.lang.psi.LocalizationConstruct.MScope;
import org.jetbrains.annotations.NotNull;

/**
 * Created with IntelliJ IDEA. User: patrick Date: 3/28/13 Time: 12:33 AM Purpose:
 */
public interface Symbol extends PsiNameIdentifierOwner, PsiReference, PsiPolyVariantReference {

  /**
   * Returns the context of a symbol which is not the correct context. It is either <code >System</code> or the explicit
   * context in a symbols name like in <code >Global`variable</code>.
   *
   * @return Context part of a symbol name
   */
  @NotNull
  String getMathematicaContext();

  /**
   * Removes a possible context part from a symbol name and returns the symbol name only. <code >Global`variable</code>
   * gives therefore <code >variable</code>.
   *
   * @return Symbol name without context part
   */
  String getSymbolName();

  /**
   * Checks if two symbols have the same name. Used for reference resolving
   *
   * @param other the other symbol
   *
   * @return true if the names match
   */
  boolean hasSameName(Symbol other);

  /**
   * Returns the full name of the symbol with context.
   *
   * @return Symbol name with context
   */
  String getFullSymbolName();

  /**
   * Returns e.g. <code>Module</code> for symbols that are localized with a Module-construct. This is mainly for the
   * highlighting.
   *
   * @return type of localization
   */
  @NotNull
  MScope getLocalizationConstruct();

}
