@echo off
echo ====================================
echo Stopping All Services
echo ====================================
echo.

docker-compose down

echo.
echo [INFO] All services stopped!
echo.
echo To remove volumes (databases):
echo   docker-compose down -v
echo.
pause
