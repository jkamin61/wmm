@echo off
REM WilliamMacMiron - Quick Setup Script for Windows
REM This script automates the setup process

echo ========================================
echo WilliamMacMiron Backend Setup
echo ========================================
echo.

REM Check Docker
echo 1. Checking Docker...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo X Docker is not installed. Please install Docker Desktop first.
    echo   Download from: https://www.docker.com/products/docker-desktop
    exit /b 1
)
echo + Docker is installed
echo.

REM Check Java
echo 2. Checking Java...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo X Java is not installed. Please install Java 17 or higher.
    exit /b 1
)
echo + Java is installed
echo.

REM Start PostgreSQL
echo 4. Starting PostgreSQL with Docker Compose...
docker-compose up -d
if %errorlevel% neq 0 (
    echo X Failed to start PostgreSQL
    echo   Make sure Docker Desktop is running
    exit /b 1
)
echo + PostgreSQL started successfully
echo.

REM Wait for PostgreSQL
echo 5. Waiting for PostgreSQL to be ready...
timeout /t 5 /nobreak >nul
echo + PostgreSQL should be ready
echo.

REM Build application
echo 6. Building application...
call mvnw.cmd clean install -DskipTests
if %errorlevel% neq 0 (
    echo X Build failed
    exit /b 1
)
echo + Application built successfully
echo.

echo ========================================
echo + Setup Complete!
echo ========================================
echo.
echo To start the application, run:
echo   mvnw.cmd spring-boot:run
echo.
echo Or use the provided start script:
echo   start.cmd
echo.
echo The application will be available at:
echo   http://localhost:8080
echo.
echo Health check:
echo   http://localhost:8080/health
echo.
echo Default admin credentials:
echo   Email: admin@williammacmiron.com
echo   Password: admin123
echo.
pause

