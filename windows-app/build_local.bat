@echo off
echo ========================================================
echo   Building Super Power Saver (Windows Laptop Edition)
echo ========================================================
pip install -r requirements.txt
pyinstaller --noconfirm --onefile --windowed --name "SuperPowerSaverWin" main.py
echo.
echo Build complete! Your standalone executable is in: dist\SuperPowerSaverWin.exe
pause
