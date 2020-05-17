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
package org.openrewrite.checkstyle.check

import org.openrewrite.checkstyle.policy.LeftCurlyPolicy
import org.junit.jupiter.api.Test
import org.openrewrite.java.JavaParser

open class LeftCurlyTest : JavaParser() {
    @Test
    fun eol() {
        val a = parse("""
            class A
            {
                {
                    if(1 == 2)
                    {
                    }
                }
            
                private static final int N = 1;
                static
                {
                }
            }
        """.trimIndent())

        val fixed = a.refactor().visit(LeftCurly.builder().build()).fix().fixed

        assertRefactored(fixed, """
            class A {
                {
                    if(1 == 2) {
                    }
                }
            
                private static final int N = 1;
                static {
                }
            }
        """)
    }

    @Test
    fun nl() {
        val a = parse("""
            class A {
                {
                    if(1 == 2) {
                    }
                }
            
                private static final int N = 1;
                static {
                }
            }
        """.trimIndent())

        val fixed = a.refactor().visit(LeftCurly.builder()
                .option(LeftCurlyPolicy.NL)
                .build()).fix().fixed

        assertRefactored(fixed, """
            class A
            {
                {
                    if(1 == 2)
                    {
                    }
                }
            
                private static final int N = 1;
                static
                {
                }
            }
        """)
    }

    @Test
    fun nlow() {
        val a = parse("""
            class A {
                {
                    if(1 == 2)
                    {
                    }
                    if(1 == 2 &&
                        3 == 4) {
                    }
                }
            
                private static final int N = 1;
                static {
                }
            }
        """.trimIndent())

        val fixed = a.refactor().visit(LeftCurly.builder()
                .option(LeftCurlyPolicy.NLOW)
                .build()).fix().fixed

        assertRefactored(fixed, """
            class A {
                {
                    if(1 == 2) {
                    }
                    if(1 == 2 &&
                        3 == 4)
                    {
                    }
                }
            
                private static final int N = 1;
                static
                {
                }
            }
        """)
    }

    @Test
    fun ignoreEnums() {
        val a = parse("""
            class A {
                {
                    switch(1) {
                    case 1:
                    {
                    }
                    case 2: {
                    }
                    }
                }
            }
        """.trimIndent())

        val fixed = a.refactor().visit(LeftCurly.builder()
                .option(LeftCurlyPolicy.EOL)
                .build()).fix().fixed

        assertRefactored(fixed, """
            class A {
                {
                    switch(1) {
                    case 1:
                    {
                    }
                    case 2: {
                    }
                    }
                }
            }
        """)
    }

    @Test
    fun dontStripNewClassInstanceInitializers() {
        assertUnchangedByRefactoring(LeftCurly.builder().build(), """
            public class JacksonUtils {
                static ObjectMapper stdConfigure(ObjectMapper mapper) {
                    return mapper
                        .registerModule(new SimpleModule() {
                            {
                                addSerializer(new InstantSerializer());
                            }
                        });
                }
            }
        """)
    }
}