@echo off
echo ====================================
echo Viewing Service Logs
echo ====================================
echo.
echo Choose a service to view logs:
echo 1. All services
echo 2. Spring Boot App
echo 3. Email Service
echo 4. MySQL
echo 5. MongoDB
echo 6. MinIO
echo.
set /p choice="Enter your choice (1-6): "

if "%choice%"=="1" (
    echo.
    echo [INFO] Showing logs for all services...
    docker-compose logs -f
) else if "%choice%"=="2" (
    echo.
    echo [INFO] Showing logs for Spring Boot App...
    docker-compose logs -f spring-app
) else if "%choice%"=="3" (
    echo.
    echo [INFO] Showing logs for Email Service...
    docker-compose logs -f email-service
) else if "%choice%"=="4" (
    echo.
    echo [INFO] Showing logs for MySQL...
    docker-compose logs -f mysql
) else if "%choice%"=="5" (
    echo.
    echo [INFO] Showing logs for MongoDB...
    docker-compose logs -f mongodb
) else if "%choice%"=="6" (
    echo.
    echo [INFO] Showing logs for MinIO...
    docker-compose logs -f minio
) else (
    echo.
    echo [ERROR] Invalid choice!
    pause
    exit /b 1
)
