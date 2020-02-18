package org.gradle.rewrite.checkstyle.check

import com.netflix.rewrite.parse.OpenJdkParser
import com.netflix.rewrite.parse.Parser
import org.junit.jupiter.api.Test

open class SimplifyBooleanExpressionTest : Parser by OpenJdkParser() {
    @Test
    fun simplifyBooleanExpression() {
        val a = parse("""
            public class A {
                {
                    boolean a = !false;
                    boolean b = (a == true);
                    boolean c = b || true;
                    boolean d = c || c;
                    boolean e = d && d;
                    boolean f = (e == true) || e;
                    boolean g = f && false;
                    boolean h = !!g;
                    boolean i = (a != false);
                }
            }
        """.trimIndent())

        val fixed = a.refactor().visit(SimplifyBooleanExpression()).fix().fixed

        assertRefactored(fixed, """
            public class A {
                {
                    boolean a = true;
                    boolean b = a;
                    boolean c = true;
                    boolean d = c;
                    boolean e = d;
                    boolean f = e;
                    boolean g = false;
                    boolean h = g;
                    boolean i = a;
                }
            }
        """)
    }
}