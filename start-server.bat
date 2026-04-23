@echo off
echo 🚀 Starting College Event Management Server
echo.

echo 📁 Navigating to server directory...
cd server

echo 🔍 Checking if Java is installed...
java -version
if %errorlevel% neq 0 (
    echo ❌ Java not found! Please install Java 17 first.
    pause
    exit /b 1
)

echo.
echo 🛑 Stopping any existing Java processes...
taskkill /f /im java.exe 2>nul
taskkill /f /im javaw.exe 2>nul

echo.
echo ⏳ Waiting 3 seconds...
timeout /t 3 /nobreak >nul

echo.
echo 🔨 Building and starting server...
echo This may take a few minutes on first run...
echo.

..\gradlew bootRun

pause