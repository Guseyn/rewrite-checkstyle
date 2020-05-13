# Rewrite Checkstyle - Eliminate Checkstyle issues. Automatically.

[![Build Status](https://circleci.com/gh/openrewrite/rewrite-checkstyle.svg?style=shield)](https://circleci.com/gh/openrewrite/rewrite-checkstyle)
[![Apache 2.0](https://img.shields.io/github/license/openrewrite/rewrite-checkstyle.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/org.openrewrite.plan/rewrite-checkstyle.svg)](https://mvnrepository.com/artifact/org.openrewrite.plan/rewrite-checkstyle)

This module checks for and auto-remediates common Checkstyle issues. The check and remediation go together, so it does _not_ use Checkstyle for the checking, but rather performs an equivalent check according to the Checkstyle documentation. Each Rewrite Checkstyle rule provides the full set of options for the corresponding Checkstyle check.

Since all of the rules check for syntactic and not semantic patterns, there is no need to ensure that the ASTs evaluated by Rewrite Checkstyle are fully type-attributed (i.e. there is no need to provide the compile classpath to `JavaParser`).

```
// produce ASTs for all Java sources to check
List<J.CompilationUnit> cus = new JavaParser()
    .setLogCompilationWarningsAndErrors(false)
    .parse(sourceSetAsCollectionOfAbsolutePaths, optionalPathToRelativizeSources)

RewriteCheckstyle checkstyleRefactoring = new RewriteCheckstyle(inputStreamToCheckstyleXml);

for(J.CompilationUnit cu : cus) {
    Change<J.CompilationUnit> fixed = rewriteCheckstyle.apply(cu.refactor()).fix();

    if(!fixed.getAllRulesThatMadeChanges().isEmpty()) {
        // can overwrite the original source file with the fixes
        Files.writeString(new File(cu.getSourcePath()).toPath(), fixed.getFixed().print());
    }
}
```

In some cases, the Rewrite Checkstyle rule is a bit "smarter" than the original Checkstyle check, and so may make
changes where Checkstyle wouldn't report an issue at all (e.g. Rewrite's `SimplifyBooleanExpression` is aware of operator associativity and precedence where Checkstyle's check is not).

# To release

`./gradlew final` or `./gradlew snapshot`

Nebula release will automatically determine the next minor release version, tag the repository,
and set the project version accordingly.