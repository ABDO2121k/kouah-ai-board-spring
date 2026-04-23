@echo off
echo Starting Spring Boot Docker environment...

echo Bringing down any existing environment (containers only)...
docker compose -f compose.yaml down

echo Building project with Maven (local)...
call mvn clean package -DskipTests

echo Building Docker images...
docker compose -f compose.yaml build

echo Starting Docker containers (detached)...
docker compose -f compose.yaml up -d

echo.
echo Environment is starting up; wait 20-40 seconds for MySQL and Minio to initialize...
echo.
echo Access the application at: http://localhost:8080
echo MySQL Database: localhost:3306
echo Minio Storage Console: http://localhost:9001 (minioadmin/minioadmin)
echo.