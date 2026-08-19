keywords: install, maven, standalone, dependency, cli
description: Install the jawk executable with one command, add Jawk from Maven Central, or run the standalone jar.

# Installation

Jawk can be installed as a `jawk` executable with one command, added as a normal Maven dependency, or used as a standalone jar for CLI execution. Every option requires a Java runtime, version 8 or later.

## One-Command Install

> [!TABS]
> * Linux / macOS
>   ```shell-session
>   $ curl -fsSL https://jawk.io/get | sh
>   ```
> * Windows (PowerShell)
>   ```powershell
>   irm https://jawk.io/get.ps1 | iex
>   ```

The installer downloads the standalone jar of the latest Jawk release from GitHub, verifies the SHA-256 checksum published with that release, and installs a `jawk` launcher:

- On Linux and macOS, the jar goes to `~/.local/share/jawk/` and the launcher to `~/.local/bin/jawk` — no `sudo` required. The installer warns with the exact line to add if `~/.local/bin` is not on your `PATH`. Set `JAWK_INSTALL_DIR` and `JAWK_DATA_DIR` to override the locations.
- On Windows, both go under `%LOCALAPPDATA%\Jawk` (override with `JAWK_INSTALL_DIR`), and the installer adds the launcher directory to your user `PATH`.

At run time, the launcher locates a Java Runtime Environment (Java 8 or later) by checking `JAWK_JAVA_HOME`, then `JAVA_HOME`, then `java` on the `PATH`, then platform-specific locations such as `/usr/libexec/java_home` on macOS and `/usr/lib/jvm` on Linux. If no suitable JRE is found, it prints a message pointing to [Adoptium](https://adoptium.net); installing or upgrading Java later requires no Jawk reinstall. The launcher also honors `JAWK_CLASSPATH`, which adds [extension](extensions.html) jars to the JVM class path.

To pin a specific release instead of the latest, set `JAWK_VERSION`:

```shell-session
$ curl -fsSL https://jawk.io/get | JAWK_VERSION=${project.version} sh
```

Prefer to inspect the script before running it? Download it first:

```shell-session
$ curl -fsSLO https://jawk.io/get
$ less get
$ sh get
```

## Project Dependency

Add Jawk to your project:

> [!TABS]
> * Maven
>   ```xml
>   <dependency>
>     <groupId>io.jawk</groupId>
>     <artifactId>jawk</artifactId>
>     <version>${project.version}</version>
>   </dependency>
>   ```
> * Gradle (Groovy)
>   ```groovy
>   implementation 'io.jawk:jawk:${project.version}'
>   ```
> * Gradle (Kotlin)
>   ```kotlin
>   implementation("io.jawk:jawk:${project.version}")
>   ```

Jawk artifacts are published on Maven Central, so standard Maven and Gradle builds can resolve them automatically. For other build tools (Ivy, SBT, Leiningen, etc.), see the [dependency information](dependency-info.html) page.

## Standalone Jar

Download [jawk-${project.version}-standalone.jar](https://github.com/jawkio/jawk/releases/download/v${project.version}/jawk-${project.version}-standalone.jar) from the [latest release](https://github.com/jawkio/jawk/releases), then run it with Java:

```shell-session
$ java -jar jawk-${project.version}-standalone.jar -?
```

## Next Steps

- [Learn the CLI](cli.html)
- [Embed Jawk in Java](java.html)
- [Load or write extensions](extensions.html)
