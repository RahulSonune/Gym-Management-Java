# FitLife Gym — full MySQL setup (schema + demo data)
# Usage:
#   .\setup-database.ps1
#   .\setup-database.ps1 -Password "YourMySqlPassword"
#   .\setup-database.ps1 -User root -Password "secret" -Fresh

param(
    [string]$User = "root",
    [string]$Password = "root",
    [switch]$Fresh
)

$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
if (-not (Test-Path $mysql)) {
    $mysql = "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
}
if (-not (Test-Path $mysql)) {
    Write-Error "mysql.exe not found. Add MySQL bin to PATH or edit this script."
    exit 1
}

$backend = Split-Path $PSScriptRoot -Parent
$migrations = Join-Path $backend "src\main\resources\db\migration"

Write-Host "Using MySQL: $mysql" -ForegroundColor Cyan
Write-Host "User: $User" -ForegroundColor Cyan

if ($Fresh) {
    Write-Host "Dropping existing database gym_management..." -ForegroundColor Yellow
    & $mysql -u $User "-p$Password" -e "DROP DATABASE IF EXISTS gym_management;"
}

Write-Host "Creating database..." -ForegroundColor Green
& $mysql -u $User "-p$Password" -e "source $($backend -replace '\\','/')/scripts/00_create_database.sql"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "Applying schema (V1)..." -ForegroundColor Green
& $mysql -u $User "-p$Password" gym_management -e "source $($migrations -replace '\\','/')/V1__schema.sql"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "Loading seed data (V2)..." -ForegroundColor Green
& $mysql -u $User "-p$Password" gym_management -e "source $($migrations -replace '\\','/')/V2__seed_data.sql"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "Registering Flyway baseline (Spring Boot integration)..." -ForegroundColor Green
Get-Content (Join-Path $backend "scripts\03_register_flyway_baseline.sql") -Raw | & $mysql -u $User "-p$Password"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host ""
Write-Host "Done! Database 'gym_management' is ready." -ForegroundColor Green
Write-Host "Tables: organization, branch, app_user, member, membership_plan, subscription, invoice, payment, attendance_log, ..." -ForegroundColor Gray
Write-Host "Login: admin@gym.com / password  OR  reception@gym.com / password" -ForegroundColor Gray
Write-Host ""
Write-Host "Update application.yml if your MySQL password is not '$Password'." -ForegroundColor Yellow
