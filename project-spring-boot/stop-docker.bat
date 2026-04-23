@echo off
echo Stopping Spring Boot Docker environment...
docker compose -f compose.yaml down

echo Environment stopped successfully!