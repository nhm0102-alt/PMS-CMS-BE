# PMS Backend (Spring Boot)

TEST LINE

## Requirements

- Java 21
- Gradle 8+ (or Gradle Wrapper)
- MySQL 8+

## Configure

Environment variables (recommended):

- `DB_URL` (default: `jdbc:mysql://localhost:3306/pms?...`)
- `DB_USERNAME` (default: `root`)
- `DB_PASSWORD` (default: empty)
- `APP_JWT_SECRET` (required for real usage; must be at least 32 bytes)
- `APP_CORS_ORIGINS` (default: `http://localhost:3000`)
- `APP_AUTH_USERNAME` / `APP_AUTH_PASSWORD` (default: `admin` / `admin`)

Example (PowerShell):

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/pms?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
$env:APP_JWT_SECRET="change-this-to-a-long-secret-at-least-32-bytes"
```

## Run

```bash
./gradlew bootRun
```

## API

- `POST /api/auth/login` → returns JWT
- `GET /api/products` (JWT required)
- `GET /api/products/{id}` (JWT required)
- `POST /api/products` (JWT required)
- `PUT /api/products/{id}` (JWT required)
- `DELETE /api/products/{id}` (JWT required)

Login example:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin\"}"
```
