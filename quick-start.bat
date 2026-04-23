@echo off
title College Event Management Server
color 0A

echo.
echo ========================================
echo   COLLEGE EVENT MANAGEMENT SERVER
echo ========================================
echo.

echo 🔍 Checking current directory...
if not exist "server\build.gradle" (
    echo ❌ Error: Not in correct project directory
    echo Please navigate to the project root directory first
    pause
    exit /b 1
)

echo ✅ Project directory found
echo.

echo 🛑 Stopping any existing servers...
taskkill /f /im java.exe 2>nul
taskkill /f /im javaw.exe 2>nul
echo.

echo 📁 Navigating to server directory...
cd server

echo 🚀 Starting server...
echo Please wait, this may take a few minutes...
echo.
echo ⏳ Starting Spring Boot application...
echo.

..\gradlew bootRun

echo.
echo ❌ Server stopped or failed to start
pause