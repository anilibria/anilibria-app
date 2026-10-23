param([Parameter(Mandatory = $true)][string]$Tv)
$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)
$adb = "$env:USERPROFILE\Downloads\platform-tools\adb.exe"
if (-not (Test-Path $adb)) { throw "Не найден adb: $adb" }
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug --console=plain
if ($LASTEXITCODE -ne 0) { throw "Ошибка сборки — установка отменена" }
& $adb connect $Tv
& $adb -s $Tv get-state
if ($LASTEXITCODE -ne 0) { throw "Телевизор недоступен" }
& $adb -s $Tv install -r ".\app\build\outputs\apk\debug\app-debug.apk"
if ($LASTEXITCODE -ne 0) { throw "Ошибка установки" }
& $adb -s $Tv shell am force-stop tv.aniliberty.next
& $adb -s $Tv shell am start -W -n tv.aniliberty.next/.MainActivity
if ($LASTEXITCODE -ne 0) { throw "Ошибка запуска" }
