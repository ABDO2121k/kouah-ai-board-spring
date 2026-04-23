@echo off
echo Updating Spring Boot Docker environment...

echo Stopping Docker containers...
docker compose -f compose.yaml down

echo Building project with Maven (local)...
call mvn clean package -DskipTests

echo Building Docker images...
docker compose -f compose.yaml build

echo Starting Docker containers...
docker compose -f compose.yaml up -d

echo.
echo Docker environment has been updated and restarted.
echo.
echo Access the application at: http://localhost:8080
echo MySQL Database: localhost:3306
echo Minio Storage Console: http://localhost:9001 (minioadmin/minioadmin)
echo.