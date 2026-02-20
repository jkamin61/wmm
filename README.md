# WilliamMacMiron Backend - Setup and Usage Guide

## Overview

WilliamMacMiron is a content management system for cataloging and reviewing spirits (whisky, etc.) with multi-language
support, tasting notes, and flavor profiles.

**Version:** 0.1.0 (Draft)  
**Tech Stack:** Java 17, Spring Boot 3.4.2, PostgreSQL 16, JWT Authentication

---

## Features Implemented

### ✅ Phase 1 & 2: Foundation + Security (COMPLETE)

1. **Database Schema**
    - Complete schema with 14+ tables
    - Multi-language support (translations)
    - Hierarchical content structure (Categories → Topics → Subtopics → Items)
    - User authentication and authorization
    - Tasting notes with flavor profiles
    - Audit logging
    - Full-text search support

2. **Security & Authentication**
    - JWT-based authentication
    - Access tokens (15 min expiry)
    - Refresh tokens (7 days expiry)
    - Password hashing (BCrypt)
    - Role-based access control (ADMIN, EDITOR, VIEWER)
    - Secure endpoints (/admin/** requires authentication)

3. **API Endpoints**
    - `GET /health` - Health check
    - `POST /auth/login` - Login and get tokens
    - `POST /auth/refresh` - Refresh access token
    - `POST /auth/logout` - Logout (revoke refresh token)
    - `GET /users/me` - Get current user info (requires authentication)

---

## Prerequisites

- **Java 17** or higher
- **Maven 3.8+**
- **Docker** (for PostgreSQL)
- **Git**

---

## Quick Start

### 1. Setup Environment Variables

Copy the example environment file and configure it:

```bash
# On Linux/Mac
cp .env.example .env

# On Windows (PowerShell)
Copy-Item .env.example .env

# On Windows (CMD)
copy .env.example .env
```

Then edit `.env` with your actual values (database credentials, JWT secret, etc.).

### 2. Start PostgreSQL with Docker

```bash
docker-compose up -d
```

This will start PostgreSQL on `localhost:5432` with the configuration from your `.env` file.

### 3. Build the Application

```bash
mvnw clean install
```

### 4. Run the Application

**Option 1: Using the start script (recommended)**

```bash
# On Linux/Mac
./start.sh

# On Windows
start.cmd
```

**Option 2: Using Maven directly**

```bash
mvnw spring-boot:run
```

The application will start on `http://localhost:8080` (or the port specified in `.env`)

### 5. Verify Setup

Check health endpoint:

```bash
curl http://localhost:8080/health
```

Expected response:

```json
{
  "success": true,
  "data": {
    "status": "UP",
    "timestamp": "2026-02-17T...",
    "application": "WilliamMacMiron",
    "version": "0.1.0"
  }
}
```

---

## Testing Authentication

### Default Admin Credentials

- **Email:** `admin@williammacmiron.com`
- **Password:** *(see `V2__seed_data.sql` — bcrypt hash, set your own in seed)*

### 1. Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@williammacmiron.com",
    "password": "YOUR_ADMIN_PASSWORD"
  }'
```

Response:

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "user": {
      "id": 1,
      "email": "admin@williammacmiron.com",
      "displayName": "Admin",
      "roles": [
        "ROLE_ADMIN"
      ]
    }
  }
}
```

### 2. Get Current User (Authenticated Request)

```bash
curl http://localhost:8080/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 3. Refresh Token

```bash
curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN"
  }'
```

### 4. Logout

```bash
curl -X POST http://localhost:8080/auth/logout \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN"
  }'
```

---

## Database Schema Highlights

### Core Tables

- **languages** - Supported languages (PL, EN)
- **users** - User accounts
- **roles** - User roles (ADMIN, EDITOR, VIEWER)
- **categories** - Top-level menu items (e.g., Whisky)
- **topics** - Second level (e.g., Scotch, Bourbon)
- **subtopics** - Third level (e.g., Islay, Speyside)
- **items** - Main content entries (individual products)
- **flavors** - Flavor dictionary (smoke, vanilla, etc.)
- **tasting_notes** - Reviews with scores
- **images** - Product images with multi-language metadata

### Translation Tables

Each content type has a corresponding `*_translations` table:

- `category_translations`
- `topic_translations`
- `subtopic_translations`
- `item_translations`
- `flavor_translations`
- etc.

### Sample Data Included

- 2 languages (Polish, English)
- 3 roles (ADMIN, EDITOR, VIEWER)
- 1 admin user
- 1 sample category (Whisky)
- 20+ flavor entries (apple, vanilla, smoke, etc.)
- System settings

---

## Configuration

### Environment Variables

All sensitive configuration is managed through environment variables in the `.env` file.

**Quick reference:**

- Copy `.env.example` to `.env` for local development
- Set production values via system environment variables or secrets manager
- Never commit `.env` files to git!

### Application Properties

Located in `src/main/resources/application.properties`

Key configurations:

- **Database:** PostgreSQL connection settings
- **JWT:** Token expiration times and secret key
- **Server:** Port 8080
- **File Upload:** Max 10MB
- **Logging:** DEBUG level for application code

### Environment Variables (Production)

For production, override these via environment variables:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET` (⚠️ CRITICAL - use a secure random key)

---

## Project Structure

```
src/main/java/com/org/wmm/
├── auth/                    # Authentication & authorization
│   ├── controller/          # AuthController
│   ├── domain/              # RefreshTokenEntity
│   ├── dto/                 # LoginRequest, AuthResponse, etc.
│   ├── repository/          # RefreshTokenRepository
│   └── service/             # AuthService
├── common/                  # Shared utilities
│   ├── constants/           # SecurityConstants, StatusConstants
│   ├── dto/                 # ApiResponse, ApiError, PageResponse
│   ├── error/               # Exception classes and handler
│   └── util/                # Utility classes
├── config/                  # Spring configuration
│   └── SecurityConfig.java  # Security & CORS configuration
├── health/                  # Health check endpoint
├── security/                # JWT implementation
│   ├── JwtTokenProvider.java
│   └── JwtAuthenticationFilter.java
├── users/                   # User management
│   ├── controller/          # UserController
│   ├── entity/              # UserEntity, RoleEntity, UserRoleEntity
│   ├── repository/          # UserRepository, RoleRepository
│   └── service/             # CustomUserDetailsService
└── WmmApplication.java      # Main application class
```

---

## Next Steps (Phase 3-6)

### Phase 3: Public Read API

- [ ] GET /public/menu - Get categories with translations
- [ ] GET /public/categories/{slug}/topics
- [ ] GET /public/topics/{slug}/items
- [ ] GET /public/items/{slug} - Item details

### Phase 4: Admin CRUD

- [ ] Category/Topic/Subtopic CRUD
- [ ] Item CRUD with publish workflow
- [ ] Audit logging integration

### Phase 5: Media & Tasting

- [ ] Image upload endpoint
- [ ] Primary image selection
- [ ] Tasting note CRUD
- [ ] Flavor profile management

### Phase 6: Search & Filtering

- [ ] Full-text search
- [ ] Advanced filtering

---

## API Documentation

### Response Format

All API responses follow this structure:

**Success Response:**

```json
{
  "success": true,
  "message": "Optional message",
  "data": {
    ...
  },
  "timestamp": "2026-02-17T..."
}
```

**Error Response:**

```json
{
  "success": false,
  "message": "Error message",
  "error": {
    "message": "Detailed error",
    "code": "ERROR_CODE",
    "status": 400,
    "details": [
      "..."
    ],
    "fieldErrors": {
      ...
    }
  },
  "timestamp": "2026-02-17T..."
}
```

### Error Codes

- `400` - Bad Request (validation errors)
- `401` - Unauthorized (invalid/expired token)
- `403` - Forbidden (insufficient permissions)
- `404` - Not Found
- `500` - Internal Server Error

---

## Security Notes

### JWT Secret Key

⚠️ **IMPORTANT:** The default JWT secret in `application.properties` is for development only!

For production:

1. Generate a secure random key (256-bit minimum)
2. Store it as an environment variable
3. Never commit it to version control

```bash
# Generate secure key (Linux/Mac)
openssl rand -base64 64

# Set as environment variable
export JWT_SECRET="your-secure-key-here"
```

### Password Requirements

- Minimum 6 characters (update validation as needed)
- Stored as BCrypt hash
- Failed login attempts tracked

---

## Troubleshooting

### Application Won't Start - Schema Validation Error

**Error:**

```
Schema-validation: missing column [device_info] in table [auth_refresh_tokens]
```

**Cause:** Your database schema doesn't match the application entities.

**Solution:** Drop all tables and let Flyway recreate them:

1. Open PgAdmin → connect to `wmm_dev` → Query Tool
2. Run: `DROP SCHEMA public CASCADE; CREATE SCHEMA public;`
3. Restart the application — Flyway will recreate everything

### Database Connection Issues

**Error:** `Connection refused` or `Could not connect to database`

**Check:**

1. PostgreSQL is running: `docker ps` or check PgAdmin
2. Credentials in `.env` file match your PostgreSQL setup
3. Database `wmm_dev` exists
4. Port 5432 is not blocked by firewall

### Compilation Errors

**Error:** `java.lang.ExceptionInInitializerError`

**Solution:**

1. Clean the project: `mvn clean`
2. Rebuild: `mvn compile`
3. Check Java version: `java -version` (should be 17+)
4. Invalidate caches in IntelliJ: File → Invalidate Caches → Restart

### Flyway Migration Errors

**Error:** `Validate failed: Migrations have failed validation`

**Solution:**

1. In PgAdmin: `DROP SCHEMA public CASCADE; CREATE SCHEMA public;`
2. Restart application to re-run migrations

### Environment Variables Not Loading

**Check:**

1. `.env` file exists in project root
2. `.env` file is not in `.gitignore` (it should be!)
3. spring-dotenv dependency is in `pom.xml`
4. Restart the application after changing `.env`

---

## License

[Add your license information]

## Contributors

[Add contributor information]

---

## Support

For issues or questions, please contact [your contact info]

