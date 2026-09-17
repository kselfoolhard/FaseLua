$ErrorActionPreference = 'Stop'
$root = Get-Location
$javaRoot = Join-Path $root 'core/src/main/java/com/modulo06/echoesmoon'
$stamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$backup = Join-Path $root ("_ECHOES_BACKUP_BOSSES_PLAYER_" + $stamp)

if (-not (Test-Path $javaRoot)) {
    Write-Host "ERRO: execute este script na raiz do projeto FaseLua." -ForegroundColor Red
    exit 1
}

New-Item -ItemType Directory -Force -Path $backup | Out-Null

$files = @(
    @{src='src/com/modulo06/echoesmoon/entities/BossCalisto.java'; dst='entities/BossCalisto.java'},
    @{src='src/com/modulo06/echoesmoon/screens/PlayerVisuals.java'; dst='screens/PlayerVisuals.java'},
    @{src='src/com/modulo06/echoesmoon/screens/BossLuaScreen.java'; dst='screens/BossLuaScreen.java'},
    @{src='src/com/modulo06/echoesmoon/screens/BossMarteScreen.java'; dst='screens/BossMarteScreen.java'},
    @{src='src/com/modulo06/echoesmoon/screens/CallistoScreen.java'; dst='screens/CallistoScreen.java'}
)

foreach ($f in $files) {
    $src = Join-Path $PSScriptRoot $f.src
    $dst = Join-Path $javaRoot $f.dst
    $backupFile = Join-Path $backup $f.dst
    New-Item -ItemType Directory -Force -Path (Split-Path $backupFile) | Out-Null

    if (Test-Path $dst) { Copy-Item $dst $backupFile -Force }
    Copy-Item $src $dst -Force
    Write-Host ("OK: " + $f.dst) -ForegroundColor Green
}

Write-Host ""
Write-Host "Backup: $backup" -ForegroundColor Cyan
Write-Host "Agora rode:" -ForegroundColor Yellow
Write-Host ".\gradlew clean :core:compileJava --console=plain" -ForegroundColor White
