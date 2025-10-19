# Setup wireless debugging for VEB_APP
# Run this AFTER connecting your phone via USB first

param(
    [Parameter(Mandatory=$true)]
    [string]$PhoneIP
)

$adbPath = "C:\Users\Jhomar\AppData\Local\Android\Sdk\platform-tools\adb.exe"

Write-Host "Setting up wireless debugging..." -ForegroundColor Green
Write-Host "Phone IP: $PhoneIP" -ForegroundColor Cyan

# Enable TCP/IP mode
Write-Host "Enabling TCP/IP mode..." -ForegroundColor Yellow
& $adbPath tcpip 5555

Start-Sleep -Seconds 2

# Connect wirelessly
Write-Host "Connecting wirelessly..." -ForegroundColor Yellow
& $adbPath connect "$PhoneIP`:5555"

Start-Sleep -Seconds 2

# Verify connection
$devices = & $adbPath devices
Write-Host "Connected devices:" -ForegroundColor Green
Write-Host $devices

if ($devices -match "$PhoneIP") {
    Write-Host "Wireless debugging setup successful!" -ForegroundColor Green
    Write-Host "You can now disconnect the USB cable." -ForegroundColor Cyan
    Write-Host "To deploy updates, just run: .\deploy.ps1" -ForegroundColor Cyan
} else {
    Write-Host "Wireless setup failed. Please check:" -ForegroundColor Red
    Write-Host "1. Phone and computer are on the same WiFi network" -ForegroundColor White
    Write-Host "2. Phone IP address is correct" -ForegroundColor White
    Write-Host "3. USB debugging is enabled" -ForegroundColor White
}
