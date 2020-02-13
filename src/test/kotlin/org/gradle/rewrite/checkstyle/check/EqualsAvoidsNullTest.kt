package org.gradle.rewrite.checkstyle.check

import com.netflix.rewrite.parse.OpenJdkParser
import com.netflix.rewrite.parse.Parser
import org.junit.jupiter.api.Test

open class EqualsAvoidsNullTest: Parser by OpenJdkParser() {
    @Test
    fun invertConditional() {
        val a = parse("""
            public class A {
                {
                    String s = null;
                    if(s.equals("test")) {}
                    if(s.equalsIgnoreCase("test")) {}
                }
            }
        """.trimIndent())

        val fixed = a.refactor().run(EqualsAvoidsNull(false)).fix()

        assertRefactored(fixed, """
            public class A {
                {
                    String s = null;
                    if("test".equals(s)) {}
                    if("test".equalsIgnoreCase(s)) {}
                }
            }
        """)
    }

    @Test
    fun removeUnnecessaryNullCheckAndParens() {
        val a = parse("""
            public class A {
                {
                    String s = null;
                    if((s != null && s.equals("test"))) {}
                    if(s != null && s.equals("test")) {}
                    if(null != s && s.equals("test")) {}
                }
            }
        """.trimIndent())

        val fixed = a.refactor().run(EqualsAvoidsNull(false)).fix()

        assertRefactored(fixed, """
            public class A {
                {
                    String s = null;
                    if("test".equals(s)) {}
                    if("test".equals(s)) {}
                    if("test".equals(s)) {}
                }
            }
        """)
    }
}