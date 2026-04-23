@echo off
echo ========================================
echo  College Event Management System
echo ========================================
echo.
echo Starting the server...
echo Please wait...
echo.

cd server
start "College Event Server" cmd /k "../gradlew.bat bootRun"

echo Waiting for server to start...
timeout /t 10 /nobreak > nul

echo.
echo Opening login page in browser...
start http://localhost:8080/login.html

echo.
echo ========================================
echo  System Started Successfully!
echo ========================================
echo.
echo Login Credentials:
echo Email: hemishaanghan1@gmail.com
echo Password: admin123
echo Role: Select Admin or Student
echo.
echo The server is running in a separate window.
echo Close that window to stop the server.
echo.
pause