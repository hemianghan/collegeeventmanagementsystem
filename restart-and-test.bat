@echo off
echo 🔄 Restarting Server and Testing Registration Count Fix
echo.

echo 📁 Navigating to server directory...
cd server

echo 🛑 Stopping any existing server processes...
taskkill /f /im java.exe 2>nul

echo ⏳ Waiting 3 seconds...
timeout /t 3 /nobreak >nul

echo 🚀 Starting server...
start "College Event Server" cmd /k "..\gradlew bootRun"

echo ⏳ Waiting 15 seconds for server to start...
timeout /t 15 /nobreak >nul

echo 🌐 Opening test page...
start http://localhost:8080/test-registration-count.html

echo 📋 Opening dashboard...
start http://localhost:8080/dashboard.html

echo.
echo ✅ Server started! 
echo.
echo 🧪 Test Instructions:
echo 1. Use the test page to verify registration count
echo 2. Login to dashboard as admin: hemishaanghan1@gmail.com / admin123
echo 3. Click "Reset Student Data" in sidebar
echo 4. Login as student: student@college.edu / any password
echo 5. Check "My Events" - should show 12 registrations
echo.
pause