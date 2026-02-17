#!/bin/bash

# WilliamMacMiron - Start Script

echo "========================================"
echo "WilliamMacMiron Backend - Starting..."
echo "========================================"

# Check if .env file exists
if [ ! -f .env ]; then
    echo "❌ ERROR: .env file not found!"
    echo "Please copy .env.example to .env and configure it:"
    echo "   cp .env.example .env"
    echo ""
    echo "Then edit .env with your actual values."
    exit 1
fi

echo "✓ .env file found"

# Load environment variables from .env
set -a
source .env
set +a

# Set profile to dev if not set
if [ -z "$SPRING_PROFILES_ACTIVE" ]; then
    export SPRING_PROFILES_ACTIVE=dev
    echo "✓ Profile set to: dev (default)"
else
    echo "✓ Profile set to: $SPRING_PROFILES_ACTIVE"
fi

# Check if PostgreSQL is running
if ! docker ps | grep -q wmm_postgres; then
    echo "⚠️  PostgreSQL is not running. Starting it now..."
    docker-compose up -d
    echo "⏳ Waiting for PostgreSQL to be ready..."
    sleep 5
fi

echo "✓ PostgreSQL is running"
echo "✓ Database: $DB_HOST:$DB_PORT/$DB_NAME"
echo "✓ Server port: ${SERVER_PORT:-8080}"
echo ""
echo "Starting Spring Boot application..."
echo "========================================"

./mvnw spring-boot:run

