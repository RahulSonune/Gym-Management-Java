# Verify Java, Maven, and MySQL for Gym Management backend
Write-Host "=== Gym Management - Environment Check ===" -ForegroundColor Cyan
Write-Host ""

# Java
$javaCmd = Get-Command java -ErrorAction SilentlyContinue
if ($javaCmd) {
    Write-Host "Java found: $($javaCmd.Source)" -ForegroundColor Green
    & java -version 2>&1 | ForEach-Object { Write-Host "  $_" }
    $versionOutput = (java -version 2>&1 | Out-String)
    if ($versionOutput -match 'version "1\.8' -or $versionOutput -match 'version "8\.') {
        Write-Host "  WARNING: Java 8 detected. This project requires JDK 17+ (Spring Boot 3)." -ForegroundColor Red
        Write-Host "  Install: https://adoptium.net/temurin/releases/?version=17" -ForegroundColor Yellow
    } elseif ($versionOutput -match 'version "1[7-9]\.' -or $versionOutput -match 'version "[2-9][0-9]\.') {
        Write-Host "  OK: Java 17+ suitable for Spring Boot 3." -ForegroundColor Green
    }
} else {
    Write-Host "Java NOT found on PATH." -ForegroundColor Red
}

$javaHome = [Environment]::GetEnvironmentVariable("JAVA_HOME", "Machine")
if (-not $javaHome) { $javaHome = [Environment]::GetEnvironmentVariable("JAVA_HOME", "User") }
if ($javaHome) {
    Write-Host "JAVA_HOME: $javaHome" -ForegroundColor Green
    if (Test-Path "$javaHome\bin\java.exe") {
        & "$javaHome\bin\java.exe" -version 2>&1 | ForEach-Object { Write-Host "  (JAVA_HOME) $_" }
    }
} else {
    Write-Host "JAVA_HOME is not set." -ForegroundColor Yellow
}

Write-Host ""

# Maven
$mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue
if ($mvnCmd) {
    Write-Host "Maven found: $($mvnCmd.Source)" -ForegroundColor Green
    mvn -version 2>&1 | Select-Object -First 3 | ForEach-Object { Write-Host "  $_" }
} elseif (Test-Path (Join-Path $PSScriptRoot "..\mvnw.cmd")) {
    Write-Host "Maven wrapper (mvnw.cmd) found in project." -ForegroundColor Green
} else {
    Write-Host "Maven NOT found. Install Maven or use IDE (IntelliJ) to run the backend." -ForegroundColor Yellow
}

Write-Host ""

# MySQL
$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
if (Test-Path $mysql) {
    Write-Host "MySQL client: $mysql" -ForegroundColor Green
    $result = & $mysql -u root -proot gym_management -e "SELECT 'DB OK' AS status, COUNT(*) AS members FROM member;" 2>&1
    $result | ForEach-Object { Write-Host "  $_" }
} else {
    Write-Host "MySQL client not found at default path." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== Next steps ===" -ForegroundColor Cyan
Write-Host "1. JDK 17+ on PATH (not Java 8 only)"
Write-Host "2. cd Gym-Management-Backend && mvn spring-boot:run"
Write-Host "3. cd Gym-Management && npm start"
Write-Host "4. Login: reception@gym.com / password"
