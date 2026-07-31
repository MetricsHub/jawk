keywords: behavior, changes, releases, versions, migration, awk, jawk
description: User-visible behavior changes in each version of Jawk, from newest to oldest.

Behavior Changes by Version
===========================

<!-- MACRO{toc|fromDepth=2|toDepth=2|id=toc} -->

This page lists the user-visible behavior changes in each version of Jawk: changes to how AWK
programs parse, execute, and produce output, plus CLI changes and breaking changes for Java
embedders. Releases that only contain internal refactoring, performance work, build, or
documentation changes are omitted. For the complete change lists, see the
[GitHub releases](https://github.com/jawkio/jawk/releases).

<!--
Maintainers: every pull request that changes user-visible behavior must add a
bullet under "Unreleased" below (see AGENTS.md). Do NOT rename that section by
hand: the release workflow (.github/workflows/release.yml) stamps it with the
released version automatically via .github/scripts/stamp-behavior-changes.sh.
-->

Unreleased
----------

- `print` outputs numeric-looking strings verbatim, as POSIX requires: `print "0100"` now prints
  `0100` (previously `100`), and input-derived values such as `$1` keep their original text.
  `OFMT` applies only when printing actual non-integral numbers, and `CONVFMT` governs
  number-to-string conversion everywhere else
  ([#529](https://github.com/jawkio/jawk/issues/529)).
- Range patterns (`begpat, endpat`) evaluate their conditions lazily: the end condition is not
  evaluated until the range has started, so conditions with side effects behave as in gawk and
  One True Awk ([#115](https://github.com/jawkio/jawk/issues/115)).
- `print (a), (b)` parses correctly: a single parenthesized expression followed by a comma
  continues the output list, a parenthesized group before `in` is a membership key, and
  `((i, j) in array)` is accepted as an expression
  ([#535](https://github.com/jawkio/jawk/issues/535)).

[v7.0.00](https://github.com/jawkio/jawk/releases/tag/v7.0.00) (2026-07-30)
----------

- The full set of gawk language extensions is now enabled by default: `BEGINFILE` / `ENDFILE`
  special rules, the `nextfile` statement, the `ERRNO` and `ARGIND` special variables,
  `@include`, `@namespace`, typed regexp literals (`@/re/`), indirect function calls (`@f()`),
  and gawk-compatible runtime array/scalar typing diagnostics.
- All gawk built-in functions are now implemented: `asort()`, `asorti()`, `typeof()`,
  `isarray()`, `mkbool()`, `gensub()`, `patsplit()`, `strtonum()`, `systime()`, `mktime()`,
  `strftime()`, `bindtextdomain()`, `dcgettext()`, `dcngettext()`, plus `SYMTAB`, `FUNCTAB`,
  and `PROCINFO["sorted_in"]`-controlled array traversal. As a consequence, these names are
  reserved by default and can no longer be used as variables (see
  [Compatibility](compatibility.html)).
- The new strict `--posix` mode disables all gawk extensions when you want the standard and
  nothing else.
- The CLI handles `--` and the `-` operand per POSIX, and passes unknown options through to the
  AWK script in `ARGV` once the program text has been supplied, which makes `#!` interpreter
  scripts work as in gawk.

[v6.4.01](https://github.com/jawkio/jawk/releases/tag/v6.4.01) (2026-06-03)
----------

- Input-derived values (fields, `getline` results, `split()` pieces) now follow POSIX
  "numeric string" (strnum) semantics: they compare numerically when both operands are numeric,
  and as strings otherwise, matching gawk and One True Awk.

[v6.2.00](https://github.com/jawkio/jawk/releases/tag/v6.2.00) (2026-05-04)
----------

- New persistent memory support: the `--persist <file>` CLI option (or the
  `JAWK_PERSISTENT_MEMORY` environment variable) saves user-defined global variables to a file
  and restores them on the next run, in the spirit of gawk's persistent memory feature.

[v6.1.00](https://github.com/jawkio/jawk/releases/tag/v6.1.00) (2026-04-19)
----------

- gawk-style arrays of arrays (`a[i][j]`) are now supported.
- New `--posix` CLI option.

[v6.0.00](https://github.com/jawkio/jawk/releases/tag/v6.0.00) (2026-04-16)
----------

- Breaking change for Java embedders: the project moved to [jawk.io](https://jawk.io), with new
  Maven coordinates and the `io.jawk` package (previously `org.metricshub.jawk`), and the Java
  API was overhauled (`AwkSink` for customized output, `InputSource` for structured input, and a
  purely behavioral `AwkSettings`).
- Fixed `print` argument list parsing (argument continuation detection).

[v5.0.00](https://github.com/jawkio/jawk/releases/tag/v5.0.00) (2025-11-04)
----------

- Java embedding API changes only (`AssocArray` became a real `java.util.Map`, refined extension
  metadata APIs); no changes to AWK script behavior.

[v4.1.00](https://github.com/jawkio/jawk/releases/tag/v4.1.00) (2025-09-29)
----------

- Removed the deprecated Jawk-specific language extensions: the `_INTEGER`, `_DOUBLE`, and
  `_STRING` typecast keywords (and the `-y` CLI flag), and the `_sleep` and `_dump` keywords
  (and the `-x` CLI flag).
- Removed the logging framework: Jawk no longer emits SLF4J messages; runtime warnings go to
  standard error.
- Environment variables with non-numeric values no longer trigger spurious
  `NumberFormatException` log messages.

[v4.0.01](https://github.com/jawkio/jawk/releases/tag/v4.0.01) (2025-08-08)
----------

- Fixed field splitting with a regex `FS` producing leading or trailing separators, and a
  tokenizer regression, raising One True Awk test-suite compatibility from 94.2% to 97.8%.

[v4.0.00](https://github.com/jawkio/jawk/releases/tag/v4.0.00) (2025-07-29)
----------

A large conformance release aligning Jawk with [One True Awk](https://github.com/onetrueawk/awk):

- Statements and control flow: `getline` without an lvalue, `exit` code handling, early-exit
  logic, pattern negation, range (condition pair) evaluation, post-increment/decrement on
  uninitialized variables and on `$` fields, newlines in `for` loops, and `^=` (power
  assignment).
- Fields and records: dynamic field numbers (`$(expr)`), `NF` tracking, `FS` parsing on empty
  lines, `split()` return count and whitespace-regex handling, and `SUBSEP` handling.
- Output: ternary expressions inside `print`, `print` argument handling, pipe output handling
  and flushing, and `ORS` flushing.
- Values and arrays: numeric string validation via `BigDecimal`, numeric detection when printing
  fields, array iteration in insertion order, array deletion with numeric keys, and array
  comparison.
- `rand()` produces a deterministic sequence for a given `srand()` seed.

[v3.3.05](https://github.com/jawkio/jawk/releases/tag/v3.3.05) (2025-04-03)
----------

- Java embedders only: packages renamed from `org.sentrysoftware.jawk` to `org.metricshub.jawk`;
  no changes to AWK script behavior.

[v3.3.04](https://github.com/jawkio/jawk/releases/tag/v3.3.04) (2025-02-13)
----------

- Fixed `sub()` with dollar references, and `sub()` / `gsub()` with `&` references in the
  replacement string.

[v3.3.03](https://github.com/jawkio/jawk/releases/tag/v3.3.03) (2024-08-29)
----------

- Fixed `gsub()` on array elements.
- Removed the spurious SLF4J connection message from standard output.

[v3.3.02](https://github.com/jawkio/jawk/releases/tag/v3.3.02) (2024-08-09)
----------

- Fixed operator associativity: most operations are left-associative, exponentiation (`^`) is
  right-associative.

[v3.3.01](https://github.com/jawkio/jawk/releases/tag/v3.3.01) (2024-08-09)
----------

- `substr()` no longer fails on out-of-range arguments.
- Output redirection (`>`) is supported within `print` statements.
- Fixed the argument order in extension function calls.

[v3.3.00](https://github.com/jawkio/jawk/releases/tag/v3.3.00) (2024-02-04)
----------

- Fixed the evaluation order in string concatenation and in function argument lists.

[v3.2.00](https://github.com/jawkio/jawk/releases/tag/v3.2.00) (2024-02-01)
----------

- `exit NN` returns the proper exit code from `BEGIN` and main rules.
- Full support for the `ORS` special variable.
- Full support for range patterns (`/start/, /end/`).
- `NR` is updated even when the program has no main rule.

[v3.1.02](https://github.com/jawkio/jawk/releases/tag/v3.1.02) (2024-01-18)
----------

- `!x` on an uninitialized variable now correctly evaluates to true.

[v3.1.01](https://github.com/jawkio/jawk/releases/tag/v3.1.01) (2024-01-17)
----------

- Fixed escaping in regexp constants, notably octal sequences.
- Improved syntax error messages.

[v3.1.00](https://github.com/jawkio/jawk/releases/tag/v3.1.00) (2024-01-17)
----------

- Newlines are allowed after `&&`, `||`, `?`, `:`, and `,`.
- Unary plus (`+a`) is supported.
- Fixed precedence of ternary and concatenation expressions; parsing and lexing follow gawk
  precedence rules for all operators.
- Fixed parsing of escape sequences in regular expression constants.
- Jawk runs on Java 8 and later.

[v3.0.00](https://github.com/jawkio/jawk/releases/tag/v3.0.00) (2023-12-14)
----------

- `printf()` and `sprintf()` use the [Printf4J](https://github.com/metricshub/printf4j) library
  for C-style formatting.
- Java embedders: packages renamed from `org.jawk` to `org.sentrysoftware.jawk`; the library is
  licensed under LGPL and released on Maven Central.

[v2.1.00 (beta)](https://github.com/jawkio/jawk/releases/tag/v2.1.00-SNAPSHOT7) (2023)
----------

- Octal (`"\033"`) and hexadecimal (`"\x1B"`) escape sequences are supported in strings.

See Also
--------

- [Compatibility and Compliance](compatibility.html)
- [GitHub releases](https://github.com/jawkio/jawk/releases)
