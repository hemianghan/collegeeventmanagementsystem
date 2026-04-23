@echo off
echo 🔍 Troubleshooting Server Issues
echo.

echo 1. Checking Java installation...
java -version
if %errorlevel% neq 0 (
    echo ❌ Java not installed or not in PATH
    echo Please install Java 17 from: https://adoptium.net/
    pause
    exit /b 1
) else (
    echo ✅ Java is installed
)

echo.
echo 2. Checking if port 8080 is in use...
netstat -an | find "8080"
if %errorlevel% equ 0 (
    echo ⚠️ Port 8080 is already in use
    echo Killing processes using port 8080...
    for /f "tokens=5" %%a in ('netstat -ano ^| find "8080" ^| find "LISTENING"') do taskkill /f /pid %%a 2>nul
) else (
    echo ✅ Port 8080 is available
)

echo.
echo 3. Checking server directory...
if exist "server\build.gradle" (
    echo ✅ Server directory found
) else (
    echo ❌ Server directory not found or invalid
    echo Make sure you're in the correct project directory
    pause
    exit /b 1
)

echo.
echo 4. Starting server...
cd server
..\gradlew bootRun

pause