@echo off
@REM  Start only infrastructure services (MySQL, MongoDB, MinIO)
@REM  Use this when you want to run Spring Boot and/or Email Service manually

echo Starting infrastructure services...


docker-compose up -d mysql mongodb minio minio-setup

echo.
echo Infrastructure services started!
echo.
echo To check status: docker-compose ps
echo To view logs: docker-compose logs -f
