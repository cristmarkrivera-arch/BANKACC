@echo off
mvn clean compile
if errorlevel 1 pause & exit /b 1
mvn exec:java
pause
