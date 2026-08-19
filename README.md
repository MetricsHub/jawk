# Jawk

![GitHub release (with filter)](https://img.shields.io/github/v/release/jawkio/jawk)
![Build](https://img.shields.io/github/actions/workflow/status/jawkio/jawk/deploy.yml)
![Reproducible](https://img.shields.io/badge/build-reproducible-green)
![GitHub top language](https://img.shields.io/github/languages/top/jawkio/jawk)
![License](https://img.shields.io/github/license/jawkio/jawk)

Jawk is a pure Java implementation of [AWK](https://en.wikipedia.org/wiki/AWK). You can run it as a CLI, embed it directly in Java applications, compile scripts once and reuse them, evaluate AWK expressions, feed it structured input, expose your own Java functions as extensions, and enable a sandboxed runtime when you need tighter execution constraints.

## Support for POSIX AWK and Gawk

Jawk fully implements POSIX AWK, and adds support for the most commonly used gawk-specific features:

- Builtins, available by default through the built-in GNU Awk compatibility extension: `asort()`, `asorti()`, `typeof()`, `isarray()`, `mkbool()`, `gensub()`, `patsplit()`, `strtonum()`, `systime()`, `mktime()`, `strftime()`, the gettext functions, and `PROCINFO["sorted_in"]`-controlled array traversal
- Arrays of arrays (`a[i][j]`), with gawk's runtime typing rules, and typed regexp literals (`@/re/`)
- Source inclusion with `@include`, namespaces with `@namespace` and `ns::name`, and indirect function calls such as `@functionName(args)`
- The `BEGINFILE` / `ENDFILE` special patterns and the `nextfile` statement, with the `ERRNO` and `ARGIND` special variables, so a script can hook into the command-line file processing loop and skip unreadable files without a fatal error
- The `IGNORECASE`, `SYMTAB`, and `FUNCTAB` special variables
- The `/dev/stdout`, `/dev/stderr`, and `/dev/stdin` special filenames in redirections and `getline`, plus `/dev/null` on Windows

The gawk-specific `@` syntax and arrays-of-arrays syntax are rejected in `--posix` mode.

See the [compatibility page](https://jawk.io/compatibility.html) for detailed behavior notes and live compatibility reports against the POSIX, One True Awk, and gawk test suites.

## Installation

Install the `jawk` command with one line; the launcher finds a Java runtime (Java 8 or later) automatically:

```shell
curl -fsSL https://jawk.io/get | sh
```

On Windows (PowerShell):

```powershell
irm https://jawk.io/get.ps1 | iex
```

See the [installation page](https://jawk.io/install.html) for details, Maven/Gradle coordinates, and the standalone jar.

## CLI Example

```shell
echo "hello world" | jawk '{ print $2 ", " $1 "!" }'
```

The CLI follows the POSIX argument conventions, and passes unknown options on to the script through `ARGV` once the program text has been supplied, as gawk does, which makes `#!` interpreter scripts work. See the [CLI documentation](https://jawk.io/cli.html) for details.

## Java Example

```java
Awk awk = new Awk();
String result = awk.script("{ print toupper($0) }").input("hello world").execute();
```

The variables passed to an embedded script may be `Map` and `List` trees, so JSON-like structures can be traversed with plain AWK array syntax. See the [Java documentation](https://jawk.io/java.html) for details.

## Documentation

- Overview: https://jawk.io/index.html
- Installation: https://jawk.io/install.html
- CLI: https://jawk.io/cli.html
- Java: https://jawk.io/java.html
- Extensions: https://jawk.io/extensions.html
- Writing Extensions: https://jawk.io/extensions-writing.html
- Compatibility: https://jawk.io/compatibility.html

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).
