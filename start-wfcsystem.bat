@echo off
setlocal
if not exist config.properties copy config.properties.example config.properties >nul
java -jar target\wfcsystem-v1-0.1.1.jar
pause
