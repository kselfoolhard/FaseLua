$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$project = Get-Location
$dst = Join-Path $project 'core/src/main/java/com/modulo06/echoesmoon/screens/PlayerVisuals.java'
$backupDir = Join-Path $project ("_ECHOES_BACKUP_PlayerVisuals_" + (Get-Date -Format 'yyyyMMdd_HHmmss'))
if (Test-Path $dst) {
    New-Item -ItemType Directory -Force -Path $backupDir | Out-Null
    Copy-Item $dst (Join-Path $backupDir 'PlayerVisuals.java') -Force
}
Copy-Item (Join-Path $root 'PlayerVisuals.java') $dst -Force
Write-Host "PlayerVisuals corrigido em: $dst"
Write-Host "Backup: $backupDir"
