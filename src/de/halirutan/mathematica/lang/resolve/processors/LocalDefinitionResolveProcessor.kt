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

package de.halirutan.mathematica.lang.resolve.processors

import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import de.halirutan.mathematica.lang.psi.LocalizationConstruct
import de.halirutan.mathematica.lang.psi.LocalizationConstruct.MScope
import de.halirutan.mathematica.lang.psi.api.FunctionCall
import de.halirutan.mathematica.lang.psi.api.Symbol
import de.halirutan.mathematica.lang.psi.api.assignment.Set
import de.halirutan.mathematica.lang.psi.api.assignment.SetDelayed
import de.halirutan.mathematica.lang.psi.api.assignment.TagSetDelayed
import de.halirutan.mathematica.lang.psi.api.pattern.Condition
import de.halirutan.mathematica.lang.psi.api.rules.Rule
import de.halirutan.mathematica.lang.psi.api.rules.RuleDelayed
import de.halirutan.mathematica.lang.psi.util.MathematicaPatternVisitor2
import de.halirutan.mathematica.lang.psi.util.PatternSymbolExtractor
import de.halirutan.mathematica.lang.resolve.SymbolResolveResult
import de.halirutan.mathematica.lang.resolve.resolvers.CompileLikeResolver
import de.halirutan.mathematica.lang.resolve.resolvers.FunctionLikeResolver
import de.halirutan.mathematica.lang.resolve.resolvers.ModuleLikeResolver
import de.halirutan.mathematica.lang.resolve.resolvers.TableLikeResolver

/**
 * Provides the functionality of resolving local references.
 *
 * "Local" means that symbols can be *bound* by constructs like `Module`, `With`, `Block`, `Plot`, `Table` or even `:=`.
 * From a very practical way, the `x` in `x_ :> x` can regarded as local and it doesn't care about other `x`s that are
 * defined outside this scope.
 *
 * This class takes care to find out where a local
 * variable was defined and it can be used to find all references of a variable inside a scope.
 *
 * This class is used by by GoToDeclaration or my reference highlighting. An example:
 *
 * Let's assume you have "Preferences" -> "Editor" -> "Highlight usages of symbol under cursor" turned on and when you
 * browse through the code and stop on a variable, the variable itself and all its usages are highlighted. The moment
 * you move the cursor over the variable, IDEA calls [Symbol.getReference] in order to find the place where this
 * variable is defined. First, it finds the correct PsiElement for which the reference should
 * be searched. Usually, this is always the [Symbol] over which the cursor is.
 *
 * The search process depends on the language; in Mathematica, I use currently the following approach: I walk the parsing tree
 * upwards and check every Module, Block, ... I find on my way. Checking means I look in the definition list whether my
 * variable is defined there. If not, I go further upwards.  On every step
 * upwards the [LocalDefinitionResolveProcessor.execute] method is called and exactly here I extract all locally
 * defined variables I find and check whether any of it has the same name as my original variable whose definition I
 * want to find.
 *
 * If I find it in any of the localization constructs like Module, Block.. I stop and return the PsiElement which is the
 * place of definition.
 *
 * @author patrick (5/22/13)
 */
class LocalDefinitionResolveProcessor(private val myStartElement: Symbol) : PsiScopeProcessor {
  /**
   * Returns the list of all symbols collected during a  run. Before returning
   * the list, it removes duplicates, so that no entry appears more than once in the auto-completion window.
   *
   * @return Sorted and cleaned list of collected symbols.
   */
  var resolveResult: SymbolResolveResult? = null

  /**
   * There are several places where a local variable can be "defined". First I check all localization constructs which
   * are always function call like `Module[{var},...]`. The complete list of localization constructs can be
   * found in [MScope].
   *
   *
   * Secondly I check the patterns in e.g. ```f[var_]:=...!```  for `SetDelayed` and `TagSetDelayed`.
   *
   *
   * Finally, `RuleDelayed` constructs are checked.
   *
   * @param element
   * Element to check for defining the [.myStartElement].
   * @param state State of the resolving.
   * @return `false` if the search can be stopped, `true` otherwise
   */
  override fun execute(element: PsiElement, state: ResolveState): Boolean {
    if (element is FunctionCall) {
      if (element.isScopingConstruct) {
        val scopingConstruct = element.scopingConstruct

        when {
          LocalizationConstruct.isModuleLike(scopingConstruct) -> resolveResult = ModuleLikeResolver().resolve(myStartElement, element, state)
          LocalizationConstruct.isFunctionLike(scopingConstruct) -> resolveResult = FunctionLikeResolver().resolve(myStartElement, element, state)
          LocalizationConstruct.isCompileLike(scopingConstruct) -> resolveResult = CompileLikeResolver().resolve(myStartElement, element, state)
          LocalizationConstruct.isTableLike(scopingConstruct) -> resolveResult = TableLikeResolver().resolve(myStartElement, element, state)
          LocalizationConstruct.isManipulateLike(scopingConstruct) -> resolveResult = TableLikeResolver().resolve(myStartElement, element, state)
        }
        resolveResult?.let { return false }
      }
    } else if (element is SetDelayed || element is TagSetDelayed || element is Set || element is Condition) {
      val patternVisitor = MathematicaPatternVisitor2()
      element.accept(patternVisitor)

      for (p in patternVisitor.patternSymbols) {
        if (p.fullSymbolName == myStartElement.fullSymbolName) {
          if (element is Set && myStartElement !== p) {
            continue
          }
          resolveResult = SymbolResolveResult(p, MScope.SETDELAYED_SCOPE, element, true)
          return false
        }
      }
    } else if (element is RuleDelayed || element is Rule) {
      val patternExtractor = PatternSymbolExtractor()
      element.accept(patternExtractor)

      for (symbol in patternExtractor.patternSymbols) {
        if (symbol.fullSymbolName == myStartElement.fullSymbolName) {
          if (element is Rule && myStartElement !== symbol) {
            continue
          }
          resolveResult = SymbolResolveResult(symbol, MScope.RULEDELAYED_SCOPE, element, true)
          return false
        }
      }
    }
    return true
  }
}
