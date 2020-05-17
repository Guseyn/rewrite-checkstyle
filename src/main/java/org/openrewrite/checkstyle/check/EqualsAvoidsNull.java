/*
 * Copyright 2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.checkstyle.check;

import org.openrewrite.Tree;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.refactor.JavaRefactorVisitor;
import org.openrewrite.java.refactor.ScopedJavaRefactorVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.openrewrite.Formatting.EMPTY;
import static org.openrewrite.Formatting.stripPrefix;

public class EqualsAvoidsNull extends JavaRefactorVisitor {
    private static final MethodMatcher STRING_EQUALS = new MethodMatcher("String equals(java.lang.Object)");
    private static final MethodMatcher STRING_EQUALS_IGNORE_CASE = new MethodMatcher("String equalsIgnoreCase(java.lang.String)");

    private final boolean ignoreEqualsIgnoreCase;

    public EqualsAvoidsNull(boolean ignoreEqualsIgnoreCase) {
        this.ignoreEqualsIgnoreCase = ignoreEqualsIgnoreCase;
    }

    public EqualsAvoidsNull() {
        this(false);
    }

    @Override
    public String getName() {
        return "checkstyle.EqualsAvoidsNull";
    }

    @Override
    public boolean isCursored() {
        return true;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public J visitMethodInvocation(J.MethodInvocation method) {
        J.MethodInvocation m = refactor(method, super::visitMethodInvocation);

        if ((STRING_EQUALS.matches(m) || (!ignoreEqualsIgnoreCase && STRING_EQUALS_IGNORE_CASE.matches(m))) &&
                m.getArgs().getArgs().get(0) instanceof J.Literal &&
                !(m.getSelect() instanceof J.Literal)) {
            Tree parent = getCursor().getParentOrThrow().getTree();
            if (parent instanceof J.Binary) {
                J.Binary binary = (J.Binary) parent;
                if (binary.getOperator() instanceof J.Binary.Operator.And && binary.getLeft() instanceof J.Binary) {
                    J.Binary potentialNullCheck = (J.Binary) binary.getLeft();
                    if ((isNullLiteral(potentialNullCheck.getLeft()) && matchesSelect(potentialNullCheck.getRight(), m.getSelect())) ||
                            (isNullLiteral(potentialNullCheck.getRight()) && matchesSelect(potentialNullCheck.getLeft(), m.getSelect()))) {
                        andThen(new RemoveUnnecessaryNullCheck(binary.getId()));
                    }
                }
            }

            m = m.withSelect(m.getArgs().getArgs().get(0).withFormatting(m.getSelect().getFormatting()))
                    .withArgs(m.getArgs().withArgs(singletonList(m.getSelect().withFormatting(EMPTY))));
        }

        return m;
    }

    private boolean isNullLiteral(Expression expression) {
        return expression instanceof J.Literal && ((J.Literal) expression).getType() == JavaType.Primitive.Null;
    }

    private boolean matchesSelect(Expression expression, Expression select) {
        return expression.printTrimmed().replaceAll("\\s", "").equals(select.printTrimmed().replaceAll("\\s", ""));
    }

    private static class RemoveUnnecessaryNullCheck extends ScopedJavaRefactorVisitor {
        public RemoveUnnecessaryNullCheck(UUID scope) {
            super(scope);
        }

        @Override
        public J visitBinary(J.Binary binary) {
            maybeUnwrapParentheses(getCursor().getParent());

            if (isScope()) {
                return stripPrefix(binary.getRight());
            }

            return super.visitBinary(binary);
        }
    }
}