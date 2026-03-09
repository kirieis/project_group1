@echo off
REM Script to run Simulator - Auto data generation
REM Make sure project is built first: mvn clean compile

echo.
echo ====================================================================
echo   Starting REAL-TIME SIMULATOR
echo ====================================================================
echo.

cd /d "%~dp0"

REM Run simulator using Maven exec plugin
mvn exec:java -Dexec.mainClass="simulator.Simulator"

pause
