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

package de.halirutan.mathematica.codeinsight.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionInitializationContext;
import de.halirutan.mathematica.codeinsight.completion.providers.*;
import org.jetbrains.annotations.NotNull;

/**
 * Provides the implementation of the extension point which is registered in /META-INF/plugin.xml. It adds all kinds of
 * completion providers to the plugin.
 *
 * @author patrick (4/2/13)
 */
public class MathematicaCompletionContributor extends CompletionContributor {

  public static final double LOCAL_VARIABLE_PRIORITY = 10000;
  public static final double GLOBAL_VARIABLE_PRIORITY = 9000;
  public static final double IMPORT_VARIABLE_PRIORITY = 8000;


  public MathematicaCompletionContributor() {
    new BuiltinFunctionCompletion().addTo(this);
    new LocalizedSymbolCompletion().addTo(this);
    new FileSymbolCompletion().addTo(this);
    new ImportedSymbolCompletion().addTo(this);
    new SmartContextAwareCompletion().addTo(this);
    new CommentCompletion().addTo(this);
  }

  @Override
  public void beforeCompletion(@NotNull CompletionInitializationContext context) {
    context.setDummyIdentifier("ZZZ");
  }


}
