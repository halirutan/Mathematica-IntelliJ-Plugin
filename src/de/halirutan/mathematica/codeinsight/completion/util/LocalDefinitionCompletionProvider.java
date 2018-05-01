/*
 * Copyright (c) 2017 Patrick Scheibe
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 *
 */

package de.halirutan.mathematica.codeinsight.completion.util;

import com.google.common.collect.Lists;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import de.halirutan.mathematica.lang.psi.LocalizationConstruct;
import de.halirutan.mathematica.lang.psi.api.FunctionCall;
import de.halirutan.mathematica.lang.psi.api.Symbol;
import de.halirutan.mathematica.lang.psi.api.assignment.Set;
import de.halirutan.mathematica.lang.psi.api.assignment.SetDelayed;
import de.halirutan.mathematica.lang.psi.api.assignment.TagSet;
import de.halirutan.mathematica.lang.psi.api.assignment.TagSetDelayed;
import de.halirutan.mathematica.lang.psi.api.rules.RuleDelayed;
import de.halirutan.mathematica.lang.psi.util.MathematicaPatternVisitor;
import de.halirutan.mathematica.lang.psi.util.MathematicaPsiUtilities;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Provides a processor to collect all variables in the current scope to suggest them for autocompletion.
 *
 * @author patrick (5/22/13)
 */
public class LocalDefinitionCompletionProvider implements PsiScopeProcessor {

  private final List<Symbol> mySymbols = Lists.newLinkedList();
  private final Symbol myStartElement;

  public LocalDefinitionCompletionProvider(Symbol startElement) {
    super();
    this.myStartElement = startElement;
  }

  @Override
  public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
    if (element instanceof Set || element instanceof SetDelayed || element instanceof TagSetDelayed || element instanceof TagSet) {
      MathematicaPatternVisitor patternVisitor = new MathematicaPatternVisitor();
      element.accept(patternVisitor);
      mySymbols.addAll(patternVisitor.getPatternSymbols());

    } else if (element instanceof FunctionCall) {
      final FunctionCall functionCall = (FunctionCall) element;
      if (functionCall.isScopingConstruct()) {
        List<Symbol> vars = Lists.newArrayList();
        final LocalizationConstruct.MScope scopingConstruct = functionCall.getScopingConstruct();

        if (LocalizationConstruct.isFunctionLike(scopingConstruct)) {
          vars = MathematicaPsiUtilities.getLocalFunctionVariables(functionCall);
        }

        if (LocalizationConstruct.isModuleLike(scopingConstruct)) {
          vars = MathematicaPsiUtilities.getLocalModuleLikeVariables(functionCall);
        }

        if (LocalizationConstruct.isTableLike(scopingConstruct)) {
          vars = MathematicaPsiUtilities.getLocalTableLikeVariables(functionCall);
        }

        if (LocalizationConstruct.isCompileLike(scopingConstruct)) {
          vars = MathematicaPsiUtilities.getLocalCompileLikeVariables(functionCall);
        }

        if (LocalizationConstruct.isManipulateLike(scopingConstruct)) {
          vars = MathematicaPsiUtilities.getLocalManipulateLikeVariables(functionCall);
        }

        if (LocalizationConstruct.isLimitLike(scopingConstruct)) {
          vars = MathematicaPsiUtilities.getLocalLimitVariables(functionCall);
        }

//      MList<Symbol> declaredSymbols = MathematicaPsiUtilities.extractLocalizedVariables(element);
        if (vars.size() > 0 && !vars.contains(myStartElement)) {
          mySymbols.addAll(vars);
        }
      }
    } else if (element instanceof RuleDelayed) {
      final List<Symbol> patternSymbols = MathematicaPsiUtilities.getSymbolsFromArgumentPattern(element.getFirstChild());
      mySymbols.addAll(patternSymbols);
    }
    return true;
  }

  /**
   * Returns the list of all symbols collected. Before returning
   * the list, it removes duplicates, so that no entry appears more than once in the auto-completion window.
   *
   * @return Sorted and cleaned list of collected symbols.
   */
  public List<Symbol> getSymbols() {

    mySymbols.sort(new SymbolComparator());
    Pattern pattern = Pattern.compile(myStartElement.getFullSymbolName().substring(0, 1) + ".*");
    Symbol tmp = null;
    Iterator<Symbol> symbolIterator = mySymbols.iterator();
    while (symbolIterator.hasNext()) {
      Symbol next = symbolIterator.next();

      if (!pattern.matcher(next.getFullSymbolName()).matches()) {
        symbolIterator.remove();
        continue;
      }

      if (tmp == null) {
        tmp = next;
        continue;
      }

      if (tmp.getFullSymbolName().equals(next.getFullSymbolName())) {
        symbolIterator.remove();
      } else {
        tmp = next;
      }

    }

    mySymbols.remove(myStartElement);
    return mySymbols;
  }

  private class SymbolComparator implements Comparator<Symbol> {

    @Override
    public int compare(Symbol o1, Symbol o2) {
      return (o1.getFullSymbolName().compareTo(o2.getFullSymbolName()));
    }
  }

}
