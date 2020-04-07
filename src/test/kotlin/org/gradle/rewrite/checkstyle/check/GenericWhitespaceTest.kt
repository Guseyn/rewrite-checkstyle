package org.gradle.rewrite.checkstyle.check

import org.openrewrite.java.JavaParser
import org.junit.jupiter.api.Test

class GenericWhitespaceTest : JavaParser() {
    @Test
    fun genericWhitespace() {
        val a = parse("""
            import java.util.*;
            public class A < T1, T2 > {
                Map < String, Integer > map;
                
                { 
                    boolean same = this.< Integer, Integer >foo(1, 2);
                    map = new HashMap <>();
                    
                    List < String > list = ImmutableList.Builder< String >::new;
                    Collections.sort(list, Comparable::< String >compareTo);
                }
                
                < K, V extends Number > boolean foo(K k, V v) {
                    return true;    
                }
            }
        """.trimIndent())

        val fixed = a.refactor().visit(GenericWhitespace()).fix().fixed

        assertRefactored(fixed, """
            import java.util.*;
            public class A<T1, T2> {
                Map<String, Integer> map;
                
                { 
                    boolean same = this.<Integer, Integer>foo(1, 2);
                    map = new HashMap<>();
                    
                    List<String> list = ImmutableList.Builder<String>::new;
                    Collections.sort(list, Comparable::<String>compareTo);
                }
                
                <K, V extends Number> boolean foo(K k, V v) {
                    return true;    
                }
            }
        """)
    }

    @Test
    fun stripUpToLinebreak() {
        val a = parse("""
            import java.util.HashMap;
            
            // extra space after 'HashMap<' and after 'Integer'
            public class A extends HashMap< 
                    String,
                    Integer 
                > {
            }
        """.trimIndent())

        val fixed = a.refactor().visit(GenericWhitespace()).fix().fixed

        assertRefactored(fixed, """
            import java.util.HashMap;
            
            // extra space after 'HashMap<' and after 'Integer'
            public class A extends HashMap<
                    String,
                    Integer
                > {
            }
        """.trimIndent())
    }

    @Test
    fun doesntConsiderLinebreaksWhitespace() {
        assertUnchangedByRefactoring(GenericWhitespace(), """
            import java.util.HashMap;
            
            public class A extends HashMap<
                    String,
                    String
                > {
            }
        """)
    }
}
