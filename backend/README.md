# SpendCheck Backend (Spring Boot 3)

The backend REST API for SpendCheck, built with Java 17 and Spring Boot 3.3.5.

## 🏗️ Architecture & Features

- **Authentication**: JWT authentication with 24-hour expiry, BCrypt password hashing, and stateless session security.
- **Expense Management**: Multi-user isolated CRUD operations with category and date range filtering.
- **Analytics**: High-performance JPQL aggregated summaries for category breakdown and monthly totals.
- **Currency Service**: Live currency conversion utilizing an in-memory 1-hour cache and API fallbacks.
- **Cloud Ready**: Configured for Neon PostgreSQL connection pooling (HikariCP 5 connections, 30s timeout), multi-stage Docker containerization, dynamic `$PORT` binding for Render.

## ⚙️ Environment Variables

| Variable | Description |
| :--- | :--- |
| `PORT` | HTTP port (default: 8080) |
| `DB_URL` | JDBC URL (e.g. `jdbc:postgresql://<neon-host>/spendcheck?sslmode=require`) |
| `DB_USERNAME` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |
| `JWT_SECRET` | 256-bit+ HMAC-SHA key |
| `JWT_EXPIRATION_MS` | Expiration (default: `86400000` = 24h) |
| `CORS_ORIGINS` | Comma-separated allowed frontend origins |

## 🧪 Running Tests

Tests use an in-memory H2 database:
```bash
./mvnw test
```

## 🚀 Running Locally

```bash
./mvnw spring-boot:run
```

## 🐳 Docker Build

```bash
docker build -t spendcheck-backend .
docker run -p 8080:8080 --env-file .env spendcheck-backend
```
