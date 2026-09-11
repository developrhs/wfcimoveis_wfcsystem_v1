@echo off
setlocal
if not exist config.properties copy config.properties.example config.properties >nul
java -jar wfcsystem-v1-0.1.0.jar
pause
