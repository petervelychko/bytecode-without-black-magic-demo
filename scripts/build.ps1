$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")

$version = (& javac -version 2>&1).ToString().Split(' ')[1]
if (-not $version.StartsWith("25")) {
    throw "This demo requires JDK 25. Found javac $version"
}

if (Test-Path target/classes) { Remove-Item -Recurse -Force target/classes }
New-Item -ItemType Directory -Force target/classes | Out-Null

$sources = Get-ChildItem -Recurse src/main/java -Filter *.java | ForEach-Object FullName
& javac --release 25 -d target/classes $sources
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "Compiled $($sources.Count) source files with JDK $version"
Write-Host "Output: target/classes"
