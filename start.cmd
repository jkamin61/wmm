@echo off
REM WilliamMacMiron - Start Script for Windows

echo ========================================
echo WilliamMacMiron Backend - Starting...
echo ========================================
echo.

REM Check if .env file exists
if not exist .env (
    echo ERROR: .env file not found!
    echo Please copy .env.example to .env and configure it:
    echo    copy .env.example .env
    echo.
    echo Then edit .env with your actual values.
    pause
    exit /b 1
)

echo [OK] .env file found

REM Load environment variables from .env
for /f "usebackq tokens=1,* delims==" %%a in (.env) do (
    set "line=%%a"
    REM Skip comments and empty lines
    if not "!line:~0,1!"=="#" (
        if not "%%a"=="" (
            set "%%a=%%b"
        )
    )
)

REM Set profile to dev if not set
if "%SPRING_PROFILES_ACTIVE%"=="" (
    set SPRING_PROFILES_ACTIVE=dev
    echo [OK] Profile set to: dev (default)
) else (
    echo [OK] Profile set to: %SPRING_PROFILES_ACTIVE%
)

REM Check if PostgreSQL is running
docker ps | findstr wmm_postgres >nul 2>&1
if %errorlevel% neq 0 (
    echo [WARN] PostgreSQL is not running. Starting it now...
    docker-compose up -d
    echo [WAIT] Waiting for PostgreSQL to be ready...
    timeout /t 5 /nobreak >nul
)

echo [OK] PostgreSQL is running
if not "%DB_HOST%"=="" echo [OK] Database: %DB_HOST%:%DB_PORT%/%DB_NAME%
if not "%SERVER_PORT%"=="" (
    echo [OK] Server port: %SERVER_PORT%
) else (
    echo [OK] Server port: 8080 (default)
)
echo.
echo Starting Spring Boot application...
echo ========================================

call mvnw.cmd spring-boot:run

