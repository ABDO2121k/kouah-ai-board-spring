@echo off
echo ====================================
echo Starting All Services
echo ====================================
echo.
echo.

REM Check if .env file exists
if not exist .env (
    echo [WARNING] .env file not found!
    echo.
    echo Please create a .env file with your SMTP credentials:
    echo   SMTP_USER=your-email@gmail.com
    echo   SMTP_PASS=your-app-password
    echo   SMTP_FROM=your-email@gmail.com
    echo.
    echo You can copy .env.example to .env and update the values.
    echo.
    pause
    exit /b 1
)

echo [INFO] Building and starting services...
docker-compose up -d --build

echo.
echo [INFO] Waiting for services to start...
timeout /t 10 /nobreak >nul

echo.
echo ====================================
echo Service Status
echo ====================================
docker-compose ps

echo.
echo ====================================
echo Services are running!
echo ====================================
echo.
echo Spring Boot API: http://localhost:8080
echo MinIO Console:   http://localhost:9001 (minioadmin/minioadmin)
echo.
echo To view logs:
echo   docker-compose logs -f
echo.
echo To stop services:
echo   docker-compose down
echo.
pause
