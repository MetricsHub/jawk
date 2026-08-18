keywords: behavior, changes, releases, versions, migration, awk, jawk
description: User-visible behavior changes in each version of Jawk, from newest to oldest.

# Behavior Changes by Version

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

## Unreleased

- A conditional expression whose result feeds a further operation on a literal — shapes like
  `(v ? v : 24) * 2`, `-(v ? 5 : 24)`, or `$(v ? 1 : 2)` — now evaluates correctly under the
  default tuple optimization. Previously the peephole literal fold merged the false branch's
  literal with the operation at the branches' join point, so the true branch skipped the
  operation, produced the false branch's folded result (`(60 ? 60 : 24) * 2` yielded 48), and
  leaked a value onto the operand stack that displaced later `print` operands. Running with
  `-s`/`--no-optimize` was unaffected
  ([#578](https://github.com/jawkio/jawk/issues/578)).
- An input-derived value whose text is a number surrounded by blanks — a record like `" 12 "`,
  a `getline var` result read from padded input, a `split()` piece under a non-default
  separator — is now recognized as a POSIX numeric string, so `$0 == 12` is true for the record
  `" 12 "`, as in gawk. Previously the blanks forced a string comparison. Text with internal
  blanks (`" 1 2 "`), trailing non-numeric text (`" 12x "`), or nothing but blanks is still not
  numeric, and string constants such as `x = " 12 "` still compare as strings
  ([#571](https://github.com/jawkio/jawk/issues/571)).
- A redirected `getline` now sets exactly the variables gawk documents for each form:
  `getline < file` and `cmd | getline` set `$0` and `NF` only, and `getline var < file` and
  `cmd | getline var` set `var` only. Previously every redirected form also advanced `NR` — so a
  side read shifted the record numbers of the whole main input — and took over `FILENAME`, and
  `getline var < file` clobbered `$0` while leaving `NF` describing the previous record. Note
  that for the pipe forms POSIX prescribes updating `NR`, but gawk documents and implements
  leaving it alone, and Jawk follows gawk
  ([#565](https://github.com/jawkio/jawk/issues/565)).
- `getline` from a file that cannot be opened, or a command that cannot be spawned, now returns
  `-1` with `ERRNO` carrying the gawk-style description (`No such file or directory`,
  `Is a directory`, ...), so the idiomatic `while ((getline line < f) > 0)` guard and probing for
  optional files work; the failed open is not cached, so a later `getline` from the same name
  retries it. Previously the underlying Java `IOException` aborted the whole script. A
  redirection name that evaluates to the empty string stays fatal, with gawk's message
  (`expression for `<' redirection has null string value`) instead of the Java exception
  ([#566](https://github.com/jawkio/jawk/issues/566)).
- A `getline` that reads nothing now leaves its target untouched, as POSIX requires: at end of
  input (return `0`) or on error (return `-1`), `getline < file` and `cmd | getline` no longer
  replace `$0` with the empty string and reset `NF` to 0, and the `var` forms — the redirected
  ones and the plain `getline var` at the end of the main input — no longer assign the empty
  string to the variable. As in gawk 4.0 and later, the subscript of an array target and the
  index of a field target are still evaluated on that path (so `getline a[++c] < f` advances
  `c`, and the reference creates the array element), only the assignment is skipped
  ([#569](https://github.com/jawkio/jawk/issues/569)).
- `getline` into a field — `getline $n < file`, `cmd | getline $n`, and the plain `getline $n` —
  now stores the record into that field, extending `NF` and rebuilding `$0` like any field
  assignment. Previously the field number was never emitted, so any of these forms corrupted the
  interpreter's operand stack and crashed the script as soon as a record was read
  ([#570](https://github.com/jawkio/jawk/issues/570)).
- On Windows, the filename `/dev/null` now designates the platform's null device (`NUL`) in
  redirections, in `getline`, and as an input operand, as gawk's Windows port does:
  `print > "/dev/null"` discards its output and creates no file, `getline < "/dev/null"` reports
  end of input (`0`), `close("/dev/null")` succeeds, and `awk '{ ... }' /dev/null` reads an empty
  input file. Previously the name was opened as a relative path: where a `dev` directory happened
  to exist on the current drive, a redirection created and truncated `dev\null` and kept the
  output that was meant to be discarded, and where it did not, the redirection was a fatal error
  and `getline` aborted the script. Only the name is translated, so `close()` still takes the
  spelling the script used, and the native Windows spelling `NUL` needs no translation — it is now
  accepted as an operand too, which the per-file input loop of `BEGINFILE`/`ENDFILE` used to
  report as a missing file. On POSIX platforms nothing changes: `/dev/null` is the device already
  ([#567](https://github.com/jawkio/jawk/issues/567)).
- The gawk special filenames `/dev/stdout`, `/dev/stderr`, `/dev/stdin` and their `/dev/fd/1`,
  `/dev/fd/2`, `/dev/fd/0` spellings are now recognized in redirections and in `getline`, and
  route to the streams the process already holds open: `print > "/dev/stdout"` writes to the
  standard output alongside unredirected `print`, `print > "/dev/stderr"` writes to the standard
  error alongside Jawk's own diagnostics (flushed per record), and `getline < "/dev/stdin"` reads
  the standard input. Previously each name was opened as a regular file, so an output redirection
  truncated it and wrote through an independent stream: with `2>log`, a script writing to
  `/dev/stderr` restarted at offset 0 of the log and clobbered what Jawk had written there (and
  vice versa). `>` and `>>` now behave identically on these names, since nothing is opened or
  truncated, and `close()` flushes the redirection and reports success without ever closing the
  underlying stream, so the name stays usable — closing a name no redirection is open on returns
  `-1`, as in gawk. Other `/dev/fd/N` names are still opened as ordinary files, an operand naming
  an input file is still opened as a regular file (use `-` for the standard input there), and
  sandbox mode still rejects every redirection, the special filenames included
  ([#556](https://github.com/jawkio/jawk/issues/556)).
- Arithmetic on integral operands is now computed in exact 64-bit integers when the result fits:
  `+`, `-`, `*`, an evenly-dividing `/`, `%`, unary `-` and `+`, `++`/`--`, and the corresponding
  compound assignments — on variables, array elements, and fields alike — keep all their digits
  beyond 2^53, and comparisons between two such values are exact as well. A compound assignment
  or `++`/`--` on an uninitialized variable, a missing array element, or a missing field starts
  from integer zero, so counters built that way stay exact. `print 9007199254740992 + 1` now prints `9007199254740993` (previously
  `9007199254740992`, which is also what gawk prints, as gawk computes in doubles unless run
  with `-M`). A result that overflows 64 bits still falls back to floating point
  (`print 9223372036854775806 + 2` prints `9223372036854775808`), and exponentiation and any
  operation with a fractional or string operand are unchanged. Integer results carry no negative
  zero: with `x = 0`, `1/-x` now prints `inf` (previously `-inf`). Programs precompiled with an
  earlier version are rejected and must be recompiled, since they may carry constants folded in
  floating point ([#537](https://github.com/jawkio/jawk/issues/537)).
- A field updated with a compound assignment or `++`/`--` now retains its numeric value, like a
  field assigned with `=` always did, instead of being replaced by its `CONVFMT` string, so
  repeated updates stay exact and `print $1` shows the full value. When `$0` is reconstituted,
  numeric field values are now converted with `CONVFMT`, as POSIX requires and gawk does:
  `{ $1 = 0.1 + 0.2; print }` prints `0.3 ...` (previously `0.30000000000000004 ...`, the raw
  Java rendering of the double) ([#537](https://github.com/jawkio/jawk/issues/537)).
- Array subscripts are now converted to their string key with the `CONVFMT` in effect at the
  moment the subscript is used, as POSIX requires and gawk does. Previously a single-dimension
  numeric subscript was stored as a number and converted lazily, so a later `CONVFMT` change
  retroactively altered existing keys: `a = 12.153; test[a] = "hi"; CONVFMT = "%.0f"` now keeps
  the key `12.153` when iterating with `for (i in test)` (previously the key became `12`). The
  same conversion now also applies to the key of the `in` operator and `delete`, so after the
  `CONVFMT` change above, `(12.153 in test)` is `0` because the lookup key converts to `12`.
  Integer subscripts within the signed 64-bit range are unaffected, and integral subscripts
  beyond that range now key as their exact digits like gawk: `a[2^63]` creates the key
  `9223372036854775808` (previously clamped to `9223372036854775807`). The "attempting to use
  an array in a scalar context" error for an array used as a subscript is now raised when the
  subscript is converted and reported on the line where the subscript starts (previously it was
  raised by the operation consuming the subscript and reported on the line of the enclosing
  array reference) ([#547](https://github.com/jawkio/jawk/issues/547)).
- Numeric constants with exponents (`2e3`, `1.5E-2`, `.5e+1`, ...) are now lexed as single
  numbers, as POSIX requires: `print 2e3` prints `2000` (previously the lexer stopped before the
  exponent, so `2e3` parsed as `2` concatenated with the uninitialized variable `e3` and printed
  `2`). As in gawk, an `e`/`E` not followed by a valid exponent is not part of the number:
  `1e` is the number `1` followed by the variable `e`
  ([#545](https://github.com/jawkio/jawk/issues/545)).
- String-to-number conversion keeps all the digits of long numeric strings instead of stopping
  after 26 characters: `s = sprintf("%d", 10^26); print s + 0` now prints
  `100000000000000004764729344` (previously `10000000000000000905969664`, a tenth of the value).
  Conversion now scans the leading numeric prefix rather than retrying progressively shorter
  prefixes, which also aligns two edge cases with gawk: number forms that only Java understands
  are no longer accepted (`"Infinity" + 0` and `"0x1p4" + 0` are `0`, previously infinity and
  `16`), while a complete *signed* infinity or NaN now converts, matched without regard to case
  (`"-inf" + 0` and `"-INF" + 0` are `-inf`, previously `0`; an unsigned `"inf"`, or a longer
  word such as `"-inform"`, remains `0`). Conversions that AWK already defined are unchanged, including
  numeric prefixes (`"25fix" + 0` is `25`), leading whitespace, and incomplete exponents
  (`"1e" + 0` is `1`) ([#545](https://github.com/jawkio/jawk/issues/545)).
- Numeric strings convert the same way wherever a whole number is expected, so `substr()`'s
  length argument now accepts every AWK number form: `substr("abcdefgh", 1, "1e1")` yields
  `abcdefgh` (previously `a`, because the length was parsed as an integer and silently truncated
  at the `e`). A length beyond the integer range yields the rest of the string instead of an
  empty one, and a padded length such as `" 3"` is honored
  ([#545](https://github.com/jawkio/jawk/issues/545)).
- An integer literal too large for a 64-bit integer is a floating-point constant, as in gawk,
  instead of aborting the run: `print 99999999999999999999999` prints `99999999999999991611392`
  (previously the parser failed with `NumberFormatException`)
  ([#545](https://github.com/jawkio/jawk/issues/545)).
- `printf` and `sprintf` are now implemented natively with POSIX AWK / gawk semantics instead of
  delegating to the Printf4J library, which emulated glibc's `printf()`
  ([#528](https://github.com/jawkio/jawk/issues/528)):
    - `%s` converts numeric values with AWK's number-to-string rules: integral values print
      without a fractional part (`printf "%s", i` after `i++` now prints `1`, not `1.0`), and
      non-integral values honor the script's current `CONVFMT` value.
    - `%c` prints the character for a numeric code point (`printf "%c", 65` prints `A`,
      previously `A` only for literal numbers, not for numeric strings or fields), or the first
      character of a string value.
    - Dynamic precision (`%.*f`) is now supported in addition to dynamic width (`%*d`), including
      negative values (negative width left-justifies, negative precision means no precision), as
      are gawk positional specifiers (`%2$s`) and the `'` grouping flag (`%'d`); mixing
      positional and sequential specifiers in one format string is a fatal error, as in gawk.
    - Out-of-range integer conversions follow gawk: negative values wrap to unsigned 64-bit for
      `%u`/`%o`/`%x`/`%X`, values beyond 64 bits print the full decimal expansion for `%d`/`%i`
      and fall back to `%g` notation for `%u`/`%o`/`%x`/`%X`.
    - NaN and infinities print as `nan`, `inf`, and `-inf` (previously Java's `NaN` /
      `Infinity`), in `print`, `printf`, and number-to-string conversions.
    - `%e`, `%f`, and `%g` round the exact binary value of the double, with halfway cases to
      even, like the C library used by gawk (`printf "%.0f", 2.5` prints `2`, previously `3`),
      and `%g` strips trailing zeros before padding (previously only when no padding applied).
      The same rounding applies to number-to-string conversions through `CONVFMT` and `OFMT`:
      with `CONVFMT="%.3f"`, `1.2345 ""` is `1.234` (previously `1.235`, from rounding the
      shortest decimal representation instead of the exact binary value, which is slightly
      below the halfway point) ([#546](https://github.com/jawkio/jawk/issues/546)).
    - `printf` with too few arguments is now a fatal error, as in gawk (previously the leftover
      specifiers were printed verbatim).
    - Unknown conversion specifiers (including `%n`, which Printf4J turned into a newline, and
      invalid length modifiers such as `ll` or `hh`) are printed verbatim without consuming an
      argument, as in gawk; the `h`, `j`, `l`, `L`, `t`, and `z` length modifiers are each
      accepted at most once and ignored.
- Integral values beyond the 64-bit range are no longer saturated to 2^63-1: `print 2^100` now
  prints the full decimal expansion `1267650600228229401496703205376` (previously
  `9223372036854775807`), and `int()` preserves such values
  ([#528](https://github.com/jawkio/jawk/issues/528)).
- New `JavaStringFormatAwkSink` for Java embedders: a sink whose `printf`/`sprintf` use Java's
  standard `String.format(...)` instead of AWK's formatting rules, giving scripts access to
  Java-only conversions such as `%,d` grouping and `%tY` date/time
  ([#528](https://github.com/jawkio/jawk/issues/528)).
- Breaking change for Java embedders: `AwkSink.printf(...)` now receives the script's current
  `CONVFMT` value as a parameter (between `ofmt` and `format`), just like it already received
  `OFMT`, and `AwkSink.sprintf(...)` now takes `CONVFMT` as its first parameter
  (`sprintf(convfmt, format, values...)`). Custom sinks must be updated to the new signatures;
  overriding `sprintf` still customizes both `printf` and `sprintf` output. The
  `org.metricshub:printf4j` dependency has been removed; its formatting logic now lives in
  `io.jawk.jrt.AwkPrintf` ([#528](https://github.com/jawkio/jawk/issues/528)).

## [v7.0.01](https://github.com/jawkio/jawk/releases/tag/v7.0.01) (2026-07-31)

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

## [v7.0.00](https://github.com/jawkio/jawk/releases/tag/v7.0.00) (2026-07-30)

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

## [v6.4.01](https://github.com/jawkio/jawk/releases/tag/v6.4.01) (2026-06-03)

- Input-derived values (fields, `getline` results, `split()` pieces) now follow POSIX
  "numeric string" (strnum) semantics: they compare numerically when both operands are numeric,
  and as strings otherwise, matching gawk and One True Awk.

## [v6.2.00](https://github.com/jawkio/jawk/releases/tag/v6.2.00) (2026-05-04)

- New persistent memory support: the `--persist <file>` CLI option (or the
  `JAWK_PERSISTENT_MEMORY` environment variable) saves user-defined global variables to a file
  and restores them on the next run, in the spirit of gawk's persistent memory feature.

## [v6.1.00](https://github.com/jawkio/jawk/releases/tag/v6.1.00) (2026-04-19)

- gawk-style arrays of arrays (`a[i][j]`) are now supported.
- New `--posix` CLI option.

## [v6.0.00](https://github.com/jawkio/jawk/releases/tag/v6.0.00) (2026-04-16)

- Breaking change for Java embedders: the project moved to [jawk.io](https://jawk.io), with new
  Maven coordinates and the `io.jawk` package (previously `org.metricshub.jawk`), and the Java
  API was overhauled (`AwkSink` for customized output, `InputSource` for structured input, and a
  purely behavioral `AwkSettings`).
- Fixed `print` argument list parsing (argument continuation detection).

## [v5.0.00](https://github.com/jawkio/jawk/releases/tag/v5.0.00) (2025-11-04)

- Java embedding API changes only (`AssocArray` became a real `java.util.Map`, refined extension
  metadata APIs); no changes to AWK script behavior.

## [v4.1.00](https://github.com/jawkio/jawk/releases/tag/v4.1.00) (2025-09-29)

- Removed the deprecated Jawk-specific language extensions: the `_INTEGER`, `_DOUBLE`, and
  `_STRING` typecast keywords (and the `-y` CLI flag), and the `_sleep` and `_dump` keywords
  (and the `-x` CLI flag).
- Removed the logging framework: Jawk no longer emits SLF4J messages; runtime warnings go to
  standard error.
- Environment variables with non-numeric values no longer trigger spurious
  `NumberFormatException` log messages.

## [v4.0.01](https://github.com/jawkio/jawk/releases/tag/v4.0.01) (2025-08-08)

- Fixed field splitting with a regex `FS` producing leading or trailing separators, and a
  tokenizer regression, raising One True Awk test-suite compatibility from 94.2% to 97.8%.

## [v4.0.00](https://github.com/jawkio/jawk/releases/tag/v4.0.00) (2025-07-29)

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

## [v3.3.05](https://github.com/jawkio/jawk/releases/tag/v3.3.05) (2025-04-03)

- Java embedders only: packages renamed from `org.sentrysoftware.jawk` to `org.metricshub.jawk`;
  no changes to AWK script behavior.

## [v3.3.04](https://github.com/jawkio/jawk/releases/tag/v3.3.04) (2025-02-13)

- Fixed `sub()` with dollar references, and `sub()` / `gsub()` with `&` references in the
  replacement string.

## [v3.3.03](https://github.com/jawkio/jawk/releases/tag/v3.3.03) (2024-08-29)

- Fixed `gsub()` on array elements.
- Removed the spurious SLF4J connection message from standard output.

## [v3.3.02](https://github.com/jawkio/jawk/releases/tag/v3.3.02) (2024-08-09)

- Fixed operator associativity: most operations are left-associative, exponentiation (`^`) is
  right-associative.

## [v3.3.01](https://github.com/jawkio/jawk/releases/tag/v3.3.01) (2024-08-09)

- `substr()` no longer fails on out-of-range arguments.
- Output redirection (`>`) is supported within `print` statements.
- Fixed the argument order in extension function calls.

## [v3.3.00](https://github.com/jawkio/jawk/releases/tag/v3.3.00) (2024-02-04)

- Fixed the evaluation order in string concatenation and in function argument lists.

## [v3.2.00](https://github.com/jawkio/jawk/releases/tag/v3.2.00) (2024-02-01)

- `exit NN` returns the proper exit code from `BEGIN` and main rules.
- Full support for the `ORS` special variable.
- Full support for range patterns (`/start/, /end/`).
- `NR` is updated even when the program has no main rule.

## [v3.1.02](https://github.com/jawkio/jawk/releases/tag/v3.1.02) (2024-01-18)

- `!x` on an uninitialized variable now correctly evaluates to true.

## [v3.1.01](https://github.com/jawkio/jawk/releases/tag/v3.1.01) (2024-01-17)

- Fixed escaping in regexp constants, notably octal sequences.
- Improved syntax error messages.

## [v3.1.00](https://github.com/jawkio/jawk/releases/tag/v3.1.00) (2024-01-17)

- Newlines are allowed after `&&`, `||`, `?`, `:`, and `,`.
- Unary plus (`+a`) is supported.
- Fixed precedence of ternary and concatenation expressions; parsing and lexing follow gawk
  precedence rules for all operators.
- Fixed parsing of escape sequences in regular expression constants.
- Jawk runs on Java 8 and later.

## [v3.0.00](https://github.com/jawkio/jawk/releases/tag/v3.0.00) (2023-12-14)

- `printf()` and `sprintf()` use the [Printf4J](https://github.com/metricshub/printf4j) library
  for C-style formatting.
- Java embedders: packages renamed from `org.jawk` to `org.sentrysoftware.jawk`; the library is
  licensed under LGPL and released on Maven Central.

## [v2.1.00 (beta)](https://github.com/jawkio/jawk/releases/tag/v2.1.00-SNAPSHOT7) (2023)

- Octal (`"\033"`) and hexadecimal (`"\x1B"`) escape sequences are supported in strings.

## See Also

- [Compatibility and Compliance](compatibility.html)
- [GitHub releases](https://github.com/jawkio/jawk/releases)
