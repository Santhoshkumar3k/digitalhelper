@echo off
TITLE DigitalHelper Launcher
echo =====================================================================
echo  DigitalHelper - Technology Made Simple for Everyone
echo  Full-Stack Spring Boot + React + MySQL Architecture
echo =====================================================================
echo.

IF "%1"=="backend" GOTO START_BACKEND
IF "%1"=="frontend" GOTO START_FRONTEND

echo Starting both Backend (Port 8080) and Frontend (Port 5173)...
echo.

start "DigitalHelper Backend" cmd /k "cd backend && "C:\Users\santhoahkumar\AppData\Local\Programs\apache-maven-3.9.6\bin\mvn.cmd" spring-boot:run"
timeout /t 6 /nobreak >nul
start "DigitalHelper Frontend" cmd /k "cd frontend && npm run dev -- --host 0.0.0.0"

echo.
echo =====================================================================
echo  Application is live and hosted on your local network!
echo =====================================================================
echo  Local:   http://localhost:5173
echo  Share with friends (same Wi-Fi/LAN):
echo           http://192.168.5.126:5173
echo =====================================================================
echo.
pause
exit /b 0

:START_BACKEND
echo Starting Spring Boot Backend on http://0.0.0.0:8080...
cd backend
"C:\Users\santhoahkumar\AppData\Local\Programs\apache-maven-3.9.6\bin\mvn.cmd" spring-boot:run
exit /b 0

:START_FRONTEND
echo Starting Vite React Frontend on http://0.0.0.0:5173...
cd frontend
npm run dev -- --host 0.0.0.0
exit /b 0
