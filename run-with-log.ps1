$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$logDir = Join-Path $projectRoot "logs"

New-Item -ItemType Directory -Force -Path $logDir | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$logFile = Join-Path $logDir "app-console-$timestamp.log"

Write-Host "Writing logs to $logFile"
Write-Host "Press Ctrl+C to stop server."

Set-Location $projectRoot
mvn spring-boot:run *>&1 | Tee-Object -FilePath $logFile -Append
