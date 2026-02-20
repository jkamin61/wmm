#!/bin/bash

# WilliamMacMiron - Quick Setup Script
# This script automates the setup process

echo "========================================"
echo "WilliamMacMiron Backend Setup"
echo "========================================"
echo ""

# Check Docker
echo "1. Checking Docker..."
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker Desktop first."
    echo "   Download from: https://www.docker.com/products/docker-desktop"
    exit 1
fi
echo "✅ Docker is installed"
echo ""

# Check Java
echo "2. Checking Java..."
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi
java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$java_version" -lt 17 ]; then
    echo "⚠️  Java version $java_version detected. Java 17+ is recommended."
else
    echo "✅ Java version $java_version is installed"
fi
echo ""

# Start PostgreSQL
echo "3. Starting PostgreSQL with Docker Compose..."
docker-compose up -d
if [ $? -eq 0 ]; then
    echo "✅ PostgreSQL started successfully"
else
    echo "❌ Failed to start PostgreSQL"
    exit 1
fi
echo ""

# Wait for PostgreSQL to be ready
echo "5. Waiting for PostgreSQL to be ready..."
sleep 5
max_attempts=30
attempt=0
while [ $attempt -lt $max_attempts ]; do
    if docker exec wmm_postgres pg_isready -U postgres > /dev/null 2>&1; then
        echo "✅ PostgreSQL is ready"
        break
    fi
    attempt=$((attempt + 1))
    echo "   Waiting... ($attempt/$max_attempts)"
    sleep 1
done

if [ $attempt -eq $max_attempts ]; then
    echo "❌ PostgreSQL did not become ready in time"
    exit 1
fi
echo ""

# Build application
echo "6. Building application..."
./mvnw clean install -DskipTests
if [ $? -eq 0 ]; then
    echo "✅ Application built successfully"
else
    echo "❌ Build failed"
    exit 1
fi
echo ""

echo "========================================"
echo "✅ Setup Complete!"
echo "========================================"
echo ""
echo "To start the application, run:"
echo "  ./mvnw spring-boot:run"
echo ""
echo "Or use the provided start script:"
echo "  ./start.sh"
echo ""
echo "The application will be available at:"
echo "  http://localhost:8080"
echo ""
echo "Health check:"
echo "  http://localhost:8080/health"
echo ""
echo "Default admin credentials:"
echo "  Email: admin@williammacmiron.com"
echo "  Password: (see V2__seed_data.sql bcrypt hash)"
echo ""

