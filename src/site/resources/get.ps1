# Jawk installer for Windows — https://jawk.io
#
# Copyright (C) 2006 - 2026 MetricsHub
# Distributed under the GNU Lesser General Public License, version 3 or
# later (LGPL-3.0-or-later); see <http://www.gnu.org/licenses/lgpl-3.0.html>.
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
# A relative override must not leave a working-directory-dependent entry on
# the user PATH: resolve it against the current location.
if (-not [System.IO.Path]::IsPathRooted($installDir)) {
    $installDir = Join-Path (Get-Location).ProviderPath $installDir
}
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

# The shim is a literal here-string so cmd metacharacters (%%, $PATH) survive.
# It locates the jar relative to its own directory (%~dp0), so the content is
# pure ASCII regardless of the install path (non-ASCII user names included)
# and the installation can be relocated as a whole.
$shim = @'
@echo off
rem Jawk launcher - https://jawk.io
rem Locates a JRE (Java 8 or later) and runs the Jawk standalone jar.
rem Set JAWK_JAVA_HOME to force a specific Java installation.

setlocal
set "JAWK_JAR=%~dp0..\jawk-standalone.jar"

if not exist "%JAWK_JAR%" (
    echo ERROR: %JAWK_JAR% not found; re-run the installer: 1>&2
    echo   irm https://jawk.io/get.ps1 ^| iex 1>&2
    exit /b 127
)

set "JAWK_JAVA="
if defined JAWK_JAVA_HOME call :try_java "%JAWK_JAVA_HOME%\bin\java.exe"
if not defined JAWK_JAVA if defined JAVA_HOME call :try_java "%JAVA_HOME%\bin\java.exe"
if not defined JAWK_JAVA for %%J in (java.exe) do if not "%%~$PATH:J" == "" call :try_java "%%~$PATH:J"

if not defined JAWK_JAVA (
    echo ERROR: no suitable Java Runtime Environment found ^(Jawk requires Java 8 or later^). 1>&2
    echo Install one from https://adoptium.net, or set JAWK_JAVA_HOME. 1>&2
    exit /b 127
)

"%JAWK_JAVA%" -jar "%JAWK_JAR%" %*
exit /b %ERRORLEVEL%

rem A candidate qualifies if it runs and does not report a 1.0-1.7 version
rem ("1.8" is Java 8 in the legacy version scheme).
:try_java
if not exist "%~1" exit /b 0
"%~1" -version >nul 2>&1 || exit /b 0
"%~1" -version 2>&1 | findstr /r /c:"version .1\.[0-7]\." >nul && exit /b 0
set "JAWK_JAVA=%~1"
exit /b 0
'@
# cmd only finds "call :label" targets in CRLF files, and the here-string
# carries whatever line endings this script was downloaded with: normalize.
$shim = ($shim -replace "`r`n", "`n") -replace "`n", "`r`n"
$shim | Set-Content -Path $shimPath -Encoding ASCII

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
