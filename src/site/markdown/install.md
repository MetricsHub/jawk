keywords: install, maven, standalone, dependency, cli
description: Install the jawk executable with one command, add Jawk from Maven Central, or run the standalone jar.

# Installation

Jawk can be installed as a `jawk` executable with one command, added as a normal Maven dependency, or used as a standalone jar for CLI execution. Every option requires a Java runtime, version 8 or later.

## Install the jawk CLI

> [!TABS]
> * Linux / macOS
>   ```shell-session
>   $ curl -fsSL https://jawk.io/get | sh
>   ```
> * Windows (PowerShell)
>   ```powershell
>   irm https://jawk.io/get.ps1 | iex
>   ```

The installer downloads the standalone jar of the latest Jawk release, verifies its published SHA-256 checksum when it can, and installs a `jawk` launcher — in `~/.local/bin` on Linux and macOS, under `%LOCALAPPDATA%\Jawk` on Windows, with no `sudo` needed. It warns you if that directory is not on your `PATH`, and `JAWK_INSTALL_DIR` changes where the launcher goes.

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

## Add Jawk to Your Java Project

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

## Download Jawk Manually

Download [jawk-${project.version}-standalone.jar](https://github.com/jawkio/jawk/releases/download/v${project.version}/jawk-${project.version}-standalone.jar) from the [latest release](https://github.com/jawkio/jawk/releases), then run it with Java:

```shell-session
$ java -jar jawk-${project.version}-standalone.jar -?
```

## Next Steps

- [Learn the CLI](cli.html)
- [Embed Jawk in Java](java.html)
- [Load or write extensions](extensions.html)
