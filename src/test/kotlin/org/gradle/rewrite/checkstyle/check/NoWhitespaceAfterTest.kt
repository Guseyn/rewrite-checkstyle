package org.gradle.rewrite.checkstyle.check

import org.openrewrite.java.JavaParser
import org.gradle.rewrite.checkstyle.policy.PunctuationToken.*
import org.junit.jupiter.api.Test

open class NoWhitespaceAfterTest : JavaParser() {
    @Test
    fun noWhitespaceAfter() {
        val notNull = """
            import java.lang.annotation.*;

            @Retention(RetentionPolicy.CLASS)
            @Target(ElementType.PARAMETER)
            public @interface NotNull {
              String value() default "";
            }
        """.trimIndent()

        val a = parse("""
            public class A {
                int m;
            
                {
                    int [] [] a;
                    int [] n = { 1, 2};
                    int [] p = {1, 2 };
                    m = n [0];
                    ++ m;
                    -- m;
                    long o = - m;
                    o = + m;
                    o = ~ m;
                    boolean b;
                    b = ! b;
                    m = (int) o;
                    new A().
                        m = 2;
                    a().
                        a();
                    var a = Function:: identity;
                }
                
                @ Override
                public boolean equals(Object o) {}
                
                int [] [] foo() { return null; }
                
                A a() { return this; }
            }
        """.trimIndent(), notNull)

        val fixed = a.refactor().visit(NoWhitespaceAfter.builder()
                .tokens(setOf(ARRAY_INIT, AT, INC, DEC, UNARY_MINUS, UNARY_PLUS, BNOT, LNOT, DOT,
                        TYPECAST, ARRAY_DECLARATOR, INDEX_OP, LITERAL_SYNCHRONIZED, METHOD_REF))
                .build()).fix().fixed

        assertRefactored(fixed, """
            public class A {
                int m;
            
                {
                    int[][] a;
                    int[] n = {1, 2};
                    int[] p = {1, 2};
                    m = n[0];
                    ++m;
                    --m;
                    long o = -m;
                    o = +m;
                    o = ~m;
                    boolean b;
                    b = !b;
                    m = (int)o;
                    new A().
                        m = 2;
                    a().
                        a();
                    var a = Function::identity;
                }
                
                @Override
                public boolean equals(Object o) {}
                
                int[][] foo() { return null; }
                
                A a() { return this; }
            }
        """)
    }

    @Test
    fun dontAllowLinebreaks() {
        val a = parse("""
            public class A {
                int m;
            
                {
                    new A().
                        m = 2;
                    a().
                        a();
                }
            }
        """.trimIndent())

        val fixed = a.refactor().visit(NoWhitespaceAfter.builder()
                .tokens(setOf(DOT))
                .allowLineBreaks(false)
                .build()).fix().fixed

        assertRefactored(fixed, """
            public class A {
                int m;
            
                {
                    new A().m = 2;
                    a().a();
                }
            }
        """)
    }

    /**
     * Evidently checkstyle doesn't recognize these as new arrays.
     */
    @Test
    fun dontChangeAnnotationValueNewArrays() {
        assertUnchangedByRefactoring(NoWhitespaceAfter.builder().build(), """
            @SuppressWarnings(value = {
                "all",
                "unchecked"
            })
            public class A {
            }
        """)
    }

    @Test
    fun dontChangeFirstAndLastValuesOfArrayInitializer() {
        assertUnchangedByRefactoring(NoWhitespaceAfter.builder().build(), """
            public class A {
                int[] ns = {
                    0,
                    1
                };
            }
        """)
    }
}
