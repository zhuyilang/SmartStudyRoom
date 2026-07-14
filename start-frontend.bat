@echo off
cd /d %~dp0
cd study-room-reservation\frontend
echo Starting frontend at http://localhost:5173
start http://localhost:5173
npm run dev
pause
