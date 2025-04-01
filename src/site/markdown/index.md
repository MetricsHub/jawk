# Jawk - AWK for Java

<!-- MACRO{toc|fromDepth=1|toDepth=2|id=toc} -->

## Introduction

Jawk is a pure Java implementation of [AWK](https://en.wikipedia.org/wiki/AWK). It executes the specified AWK scripts to parse and process text input, and generate a text output. Jawk can be used as a CLI, but more importantly it can be invoked from within your Java project.

This project is forked from the excellent [Jawk project](https://jawk.sourceforge.net/) that was maintained by [hoijui on GitHub](https://github.com/hoijui/Jawk).

## Run Jawk CLI

It's very simple:

* Download [jawk-${project.version}-standalone.jar](https://github.com/metricshub/Jawk/releases/download/v${project.version}/jawk-${project.version}-standalone.jar) from the [latest release](releases)
* Make sure to have Java installed on your system ([download](https://adoptium.net/))
* Execute `jawk-${project.version}-standalone.jar` just like the "traditional" AWK

See [usage and examples of Jawk CLI](cli.html).

## Run AWK inside Java

See [AWK in Java documentation](java.html).

## Features

As stated earlier, **Jawk** interprets AWK scripts in Java. This is a full implementation of AWK, which includes:

* An intuitive text processing paradigm, tightly integrated with regular expressions.
* Functions with local, static scoping.
* Scalar and associative array (map) variables.
* Weakly typed variables for greatest flexibility with automatic string/number conversion.
* Powerful IPC constructs similar to those used by most UNIX shells (pipes and IO redirect).
* Highly intuitive error diagnostics.

**Jawk** also offers the following features which the original AWK does not provide:

* Output to a post-compiled, pre-interpreted format for both elimination of the compilation step and obfuscation of **Jawk** scripts.
* Text dumps of abstract syntax tree and intermediate code representation (tuples).
* Maintenance of associative arrays in key-sorted order.
* Error detection for printf/sprintf format parameters (via the -r argument).
* An opt-in, flexible extension facility with event blocking capabilities.

Because we're using Java, the following differences exist in order to blend easily within the Java environment:

* **Jawk** regular expressions are implemented with Java regular expressions. Therefore, they differ from AWK's regular expression semantics (mostly by adding functionality over AWK's regular expressions).
* printf/sprintf formatting is done by java.util.Formatter. This is markedly different from C's, and thus AWK's printf(). Java's Formatter class does not attempt to implicitly convert its argument datatypes. If differing datatypes are present than what is expected, an IllegalFormatException will occur. Therefore, the script developer must either keep track of implicit type conversions by Jawk, or use \_DOUBLE, \_INTEGER, and _STRING keywords to forcibly convert parameters to these types (to do so, the -y parameter must be provided). An example is provided below:

    ```awk
    BEGIN {
        a=3
        # java.util.Formatter accepts %n as \\n
        printf("Integer of a = %02d%n", _INTEGER a)
        printf("Double of a = %.2f%n", _DOUBLE a)
        printf("String of a = %s%n", _STRING a)
    }
    ```

    Without the -y argument, only "String of a = 3" is displayed because `_STRING` is evaluated to a blank string, and the concatenation of two expressions results in a string. With the -y argument, all three lines are displayed.

### Differences with the original Jawk

There's a growing list of things that make our version diverge from the original Jawk written by Danny Daglas, and maintained by Robin Vobruba:

* Replaced [Logback](https://logback.qos.ch/) with the [SimpleLogger](https://www.slf4j.org/api/org/slf4j/simple/SimpleLogger.html) that comes with [SLF4J](https://www.slf4j.org/)
* Removed the AWK-to-JVM bytecode compiler
* Removed the Socket extension (to get a smaller jar)
* Improved performance in parsing inputs and printed output
* Support for long integers
* Support for octal and hexadecimal notation in strings (allowing `ESC` characters to do fancy terminal effects)
* Artifact *groupId* and package is `org.metricshub`
* Added gawk's suite of unit tests
* Added bwk's suite of unit tests
* License is LGPL for the Maven artifact

### Differences with other AWKs

Other versions of AWK will run through a script and issue a "runtime error" if a user-defined function is not found. **Jawk** does not. It attempts to resolve all function calls to defined functions at _compile-time_ (after parsing the script and prior to assembling the intermediate code from the abstract syntax tree). This is necessary in order to produce intermediate code with branch statements fully resolved.

Other versions of AWK provide command-line parameters to choose compile-time or run-time checks for function name resolution. **Jawk** does not, mainly to ensure semantic analysis is done for the reasons stated above. Also, to undo these semantic checks will result in unresolved references, most likely resulting in NullPointerExceptions.

Other semantic checks include formal/actual parameter analysis and array/scalar operation verification. Again, these are necessary to produce coherent intermediate code.
