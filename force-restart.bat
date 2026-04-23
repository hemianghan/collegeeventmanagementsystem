@echo off
echo 🛑 Force stopping all Java processes...
taskkill /f /im java.exe 2>nul
taskkill /f /im javaw.exe 2>nul

echo ⏳ Waiting 5 seconds...
timeout /t 5 /nobreak >nul

echo 🧹 Cleaning build cache...
cd server
call ..\gradlew clean

echo 🔨 Building fresh...
call ..\gradlew build

echo 🚀 Starting server...
call ..\gradlew bootRun

pause