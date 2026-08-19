keywords: extensions, plugins, functions, loading
description: Load and use Jawk extensions from the CLI or the Java API.

# Using Extensions

<!-- MACRO{toc|fromDepth=2|toDepth=3|id=toc} -->

Jawk extensions let Java code expose additional AWK-callable functions to a script. By default, Jawk enables the built-in [GNU Awk compatibility extension](#gawk), so gawk functions such as `asort()` and `typeof()` work out of the box. Every other extension is opt-in, through one of two loading paths:

- CLI: `-l <extension>` or `--load <extension>`
- Java API: pass extension instances to an `Awk` constructor

Either one replaces the default extension set, so add `GawkExtension` to the list when the script still needs the gawk functions. A script cannot load an extension by itself: what it may call is always the host's or the command line's decision.

## List Available Extensions

From the CLI, print the currently registered extension identifiers:

```shell-session
$ jawk --list-ext
```

From Java, inspect the registry directly:

```java
Awk.listAvailableExtensions().forEach((name, extension) ->
        System.out.println(name + " -> " + extension.getClass().getName()));
```

The registry may expose multiple identifiers for the same implementation, for example a registered name, a simple class name, and a fully qualified class name.

Note that `--list-ext` must be used by itself and only shows what is already registered in the JVM, so a fresh CLI run lists exactly the built-in extensions: a custom extension jar on the classpath does not appear there. Load it with `-l` and its fully qualified class name, as described below.

## Load Extensions from the CLI

Load an extension with any supported identifier:

```shell-session
$ jawk -l stdin -f script.awk
```

```shell-session
$ jawk -l io.jawk.ext.StdinExtension -f script.awk
```

If the extension class is not already registered, the CLI can still resolve it by fully qualified class name as long as the class is available on the JVM classpath. With the `jawk` launcher installed by the [one-command installer](install.html), add extension jars to the classpath by setting `JAWK_CLASSPATH`:

```shell-session
$ JAWK_CLASSPATH=my-extension.jar jawk -l com.company.my.SampleExtension -f script.awk
```

When running the standalone jar with `java` directly, remember that `java -jar` ignores `-cp`, `-classpath`, and the `CLASSPATH` environment variable: put both jars on the class path and name the main class explicitly, as in `java -cp "my-extension.jar:jawk-${project.version}-standalone.jar" io.jawk.Cli ...` (with `;` instead of `:` on Windows). See [Writing Extensions](extensions-writing.html) for a complete example.

## Load Extensions from Java

Pass extension instances directly to `Awk`:

```java
Awk awk = new Awk(StdinExtension.INSTANCE, new MyExtension());
```

That keeps extension availability explicit and local to the embedding code.

## Built-In Extensions

<a id="gawk"></a>
### GNU Awk Compatibility (Enabled by Default)

`GawkExtension` implements gawk-specific builtins and belongs to the default extension set, so its functions are available whenever no explicit extension list is supplied:

- `asort(source [, dest [, how]])` sorts an array by value and renumbers the result with integer indices starting at 1
- `asorti(source [, dest [, how]])` sorts an array by index instead of by value
- `typeof(x)` returns the gawk type category of a value: `"number"`, `"string"`, `"strnum"`, `"array"`, `"regexp"`, `"number|bool"`, `"unassigned"`, or `"untyped"`
- `isarray(x)` returns 1 when the value is an array, 0 otherwise
- `mkbool(expression)` creates a gawk-style boolean-typed number
- `gensub(regexp, replacement, how [, target])` returns the substituted text without modifying the target
- `patsplit(string, array [, fieldpat [, seps]])` splits by content: pieces matching `fieldpat` (default: the `FPAT` variable, or any non-whitespace run) become fields, and the text between them lands in `seps[0]`..`seps[n]`. Note that Java regular expressions pick the first matching alternative rather than the POSIX longest one, so order alternatives longest-first
- `strtonum(str)` converts a string to a number, recognizing gawk's `0x` hexadecimal and leading-zero octal notation
- `systime()` returns the current time in seconds since the epoch
- `mktime(datespec [, utc-flag])` converts a `"YYYY MM DD HH MM SS [DST]"` specification into seconds since the epoch, normalizing out-of-range values, or returns -1 when the specification is invalid
- `strftime([format [, timestamp [, utc-flag]]])` formats a timestamp with the C `strftime(3)` conversion specifiers (C-locale English names), including the GNU padding, case, and field-width flags (`%-d`, `%_d`, `%^a`, `%5d`); the format defaults to `PROCINFO["strftime"]` or gawk's `"%a %b %e %H:%M:%S %Z %Y"`
- `bindtextdomain(directory [, domain])`, `dcgettext(string [, domain [, category]])`, and `dcngettext(string1, string2, number [, domain [, category]])` implement gawk's internationalization interface; since Jawk ships no message catalogs, they behave exactly like gawk without a matching `.mo` file: text is returned untranslated and `dcngettext()` applies the English plural rule

`mktime()` and `strftime()` honor `ENVIRON["TZ"]` and follow the Java platform's time zone data and calendar rules; see [date and time functions](compatibility.html#date-and-time-functions) for the edge cases where this departs from gawk's C-library behavior.

`asort()`, `asorti()`, and the `for (index in array)` statement honor `PROCINFO["sorted_in"]` with gawk's predefined comparison modes: `@unsorted`, `@ind_str_asc`, `@ind_num_asc`, `@val_str_asc`, `@val_num_asc`, `@val_type_asc`, and their `_desc` counterparts. String comparisons ignore case when `IGNORECASE` is non-zero.

Beyond these functions, the interpreter itself implements gawk's `BEGINFILE` / `ENDFILE` special patterns, the `nextfile` statement, the `ERRNO` and `ARGIND` special variables, the `SYMTAB` and `FUNCTAB` arrays, and gawk's source-level `@` syntax:

```awk
@include "library.awk"
@namespace "report"

function render(value) { return value }

BEGIN {
    callback = "report::render"
    print @callback(42)
}
```

None of that depends on this extension. POSIX mode keeps the `nextfile` statement, as `gawk --posix` does, but turns `BEGINFILE` and `ENDFILE` into ordinary identifiers, along with `ERRNO` and `ARGIND`, leaves `SYMTAB` and `FUNCTAB` unpopulated, and rejects every `@` form. See [gawk source syntax](compatibility.html#gawk-source-syntax), [BEGINFILE and ENDFILE](compatibility.html#beginfile-and-endfile), and [SYMTAB and FUNCTAB](compatibility.html#symtab-and-functab) for the details.

> [!NOTE]
> Because these functions are registered by default, `gensub`, `typeof`, `isarray`, `asort`, `asorti`, `mkbool`, `patsplit`, `strtonum`, `systime`, `mktime`, `strftime`, `bindtextdomain`, `dcgettext`, and `dcngettext` become reserved function names. A script that uses them as variable or function identifiers must be run with an explicit extension list that omits `GawkExtension`.

The registry exposes the extension through identifiers such as:

- `GawkExtension`
- `io.jawk.ext.GawkExtension`
- `GNU Awk Compatibility`

### Stdin Support

The built-in registry also includes the stdin extension, which is exposed through identifiers such as:

- `stdin`
- `StdinExtension`
- `io.jawk.ext.StdinExtension`

It provides three functions for scripts that must consume standard input without blocking the whole
run: `StdinHasInput()` reports whether a read can proceed, `StdinGetline()` reads the next line, and
`StdinBlock()` returns a block object that waits for standard input to become readable.

## Sandbox Interaction

Sandboxing restricts what a script may do at run time; it does not change how extensions are loaded. For a sandboxed Java embedding, construct `SandboxedAwk` with the extension instances you want to allow. For a sandboxed CLI run, combine `-S` with the `-l` options you want to preload.

## See Also

- [Writing Extensions](extensions-writing.html)
- [Java Quickstart](java.html)
- [CLI Quickstart](cli.html)
