@echo off
cd /d %~dp0
cd study-room-reservation\backend\target
echo Starting backend...
java -jar study-room-backend-1.0.0.jar
pause
