@echo off
cd /d "%~dp0"
echo Dang nap Token va khoi dong Ngrok...
:: Dong nay de nap token vao ban ngrok moi
.\ngrok.exe config add-authtoken 39AA85xOmoZDldz9GTNwPuNu0rz_5Byfz4GfpsxFg3pzJADXX
:: Dong nay de chay server
.\ngrok.exe http 127.0.0.1:8081 --domain=unplanked-inculpably-malorie.ngrok-free.dev
pause