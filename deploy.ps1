# Auto-deploy script for VEB_APP
# This script will build and install the APK to your connected device

Write-Host "Building VEB_APP..." -ForegroundColor Green
.\gradlew assembleDebug

if ($LASTEXITCODE -eq 0) {
    Write-Host "Build successful! Checking for connected devices..." -ForegroundColor Green
    
    $adbPath = "C:\Users\Jhomar\AppData\Local\Android\Sdk\platform-tools\adb.exe"
    $apkPath = "app\build\outputs\apk\debug\app-debug.apk"
    
    # Check for connected devices
    $devices = & $adbPath devices | Select-String "device$"
    
    if ($devices) {
        Write-Host "Found connected device(s). Installing APK..." -ForegroundColor Yellow
        & $adbPath install -r $apkPath
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "APK installed successfully!" -ForegroundColor Green
            Write-Host "You can now disconnect your phone and use wireless debugging." -ForegroundColor Cyan
        } else {
            Write-Host "Installation failed. Please check your device connection." -ForegroundColor Red
        }
    } else {
        Write-Host "No devices connected. Please:" -ForegroundColor Red
        Write-Host "1. Connect your phone via USB" -ForegroundColor White
        Write-Host "2. Enable USB Debugging in Developer Options" -ForegroundColor White
        Write-Host "3. Run this script again" -ForegroundColor White
        Write-Host ""
        Write-Host "APK file is ready at: $apkPath" -ForegroundColor Cyan
    }
} else {
    Write-Host "Build failed!" -ForegroundColor Red
}
