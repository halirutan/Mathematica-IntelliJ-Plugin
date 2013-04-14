/*
 * Mathematica Plugin for Jetbrains IDEA
 * Copyright (C) 2013 Patrick Scheibe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.halirutan.mathematica.parsing.psi.impl.string;

import com.intellij.lang.ASTNode;
import de.halirutan.mathematica.parsing.psi.impl.ExpressionImpl;
import org.jetbrains.annotations.NotNull;

/**
 * @author patrick (4/14/13)
 */
public class StringExpressionImpl extends ExpressionImpl {
    public StringExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }
}