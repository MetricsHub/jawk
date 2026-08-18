# Jawk installer for Windows — https://jawk.io
#
# Usage (PowerShell):
#   irm https://jawk.io/get.ps1 | iex
#
# Environment variables:
#   JAWK_VERSION      Pin a specific release (e.g. "3.5.0"); default: latest.
#   JAWK_INSTALL_DIR  Installation directory; default: %LOCALAPPDATA%\Jawk.
#
# The installer downloads the standalone jar of the requested Jawk release
# from GitHub, verifies its SHA-256 checksum, writes a jawk.cmd shim that
# locates a JRE (Java 8 or later) at run time, and adds the shim directory
# to the user PATH.

$ErrorActionPreference = 'Stop'

$repo = 'jawkio/jawk'
$version = if ($env:JAWK_VERSION) { $env:JAWK_VERSION } else { 'latest' }
$installDir = if ($env:JAWK_INSTALL_DIR) { $env:JAWK_INSTALL_DIR } else { Join-Path $env:LOCALAPPDATA 'Jawk' }
$binDir = Join-Path $installDir 'bin'
$jarPath = Join-Path $installDir 'jawk-standalone.jar'
$shimPath = Join-Path $binDir 'jawk.cmd'

# --- Resolve the download URLs ----------------------------------------------

# Releases publish a version-less "jawk-standalone.jar" alias, which makes
# "latest" resolvable without any API call. Releases older than that alias
# are still reachable through their versioned asset name, resolved below.
if ($version -eq 'latest') {
    $jarUrl = "https://github.com/$repo/releases/latest/download/jawk-standalone.jar"
} else {
    $jarUrl = "https://github.com/$repo/releases/download/v$version/jawk-$version-standalone.jar"
}

# --- Download the jar and verify its checksum --------------------------------

$tmpJar = Join-Path ([System.IO.Path]::GetTempPath()) "jawk-standalone-$PID.jar"
Write-Host "Downloading $jarUrl ..."
try {
    Invoke-WebRequest -UseBasicParsing -Uri $jarUrl -OutFile $tmpJar
} catch {
    if ($version -ne 'latest') {
        throw "Download failed: check that release v$version exists and that you are online."
    }
    # The latest release predates the version-less alias: resolve its version
    # through the GitHub API and retry with the versioned asset name.
    $release = Invoke-RestMethod -UseBasicParsing -Uri "https://api.github.com/repos/$repo/releases/latest"
    $version = $release.tag_name -replace '^v', ''
    if (-not $version) { throw 'Could not determine the latest Jawk release version.' }
    $jarUrl = "https://github.com/$repo/releases/download/v$version/jawk-$version-standalone.jar"
    Write-Host "Downloading $jarUrl ..."
    Invoke-WebRequest -UseBasicParsing -Uri $jarUrl -OutFile $tmpJar
}

try {
    $expectedSum = (Invoke-RestMethod -UseBasicParsing -Uri "$jarUrl.sha256") -split '\s+' | Select-Object -First 1
} catch {
    $expectedSum = $null
    Write-Host 'WARNING: no checksum published for this release; skipping verification.'
}
if ($expectedSum) {
    $actualSum = (Get-FileHash -Algorithm SHA256 -Path $tmpJar).Hash
    if ($actualSum -ne $expectedSum) {
        Remove-Item -Force $tmpJar
        throw "Checksum mismatch: expected $expectedSum, got $actualSum. Aborting."
    }
    Write-Host 'Checksum verified.'
}

# --- Install the jar and the shim ---------------------------------------------

New-Item -ItemType Directory -Force -Path $binDir | Out-Null
Move-Item -Force $tmpJar $jarPath

# The shim is a literal here-string so cmd metacharacters (%%, $PATH) survive;
# the jar path is substituted through a placeholder afterwards.
$shim = @'
@echo off
rem Jawk launcher - https://jawk.io
rem Locates a JRE (Java 8 or later) and runs the Jawk standalone jar.
rem Set JAWK_JAVA_HOME to force a specific Java installation.

setlocal
set "JAWK_JAR=__JAWK_JAR__"

if not exist "%JAWK_JAR%" (
    echo ERROR: %JAWK_JAR% not found; re-run the installer: 1>&2
    echo   irm https://jawk.io/get.ps1 ^| iex 1>&2
    exit /b 127
)

set "JAWK_JAVA="
if defined JAWK_JAVA_HOME if exist "%JAWK_JAVA_HOME%\bin\java.exe" set "JAWK_JAVA=%JAWK_JAVA_HOME%\bin\java.exe"
if not defined JAWK_JAVA if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAWK_JAVA=%JAVA_HOME%\bin\java.exe"
if not defined JAWK_JAVA for %%J in (java.exe) do if not "%%~$PATH:J" == "" set "JAWK_JAVA=%%~$PATH:J"

if not defined JAWK_JAVA (
    echo ERROR: no Java Runtime Environment found ^(Jawk requires Java 8 or later^). 1>&2
    echo Install one from https://adoptium.net, or set JAWK_JAVA_HOME. 1>&2
    exit /b 127
)

"%JAWK_JAVA%" -jar "%JAWK_JAR%" %*
exit /b %ERRORLEVEL%
'@
$shim.Replace('__JAWK_JAR__', $jarPath) | Set-Content -Path $shimPath -Encoding ASCII

Write-Host "Installed the Jawk jar to $jarPath"
Write-Host "Installed the jawk launcher to $shimPath"

# --- PATH registration ---------------------------------------------------------

$userPath = [Environment]::GetEnvironmentVariable('Path', 'User')
$onPath = ($userPath -split ';' | Where-Object { $_ -eq $binDir }).Count -gt 0
if (-not $onPath) {
    $newPath = if ($userPath) { "$userPath;$binDir" } else { $binDir }
    [Environment]::SetEnvironmentVariable('Path', $newPath, 'User')
    Write-Host "Added $binDir to your user PATH."
    Write-Host "Open a new terminal, then run 'jawk -?' to get started."
} else {
    Write-Host "Run 'jawk -?' to get started."
}
