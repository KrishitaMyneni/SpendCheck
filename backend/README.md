# SpendCheck Backend

Spring Boot 3 RESTful API backend for SpendCheck, built with Java 17, Maven, Spring Data JPA, Spring Security, and PostgreSQL.

---

## 🏗️ Architecture & Project Structure

The backend follows standard layered enterprise Spring Boot conventions:

```
backend/
├── pom.xml                               # Maven project definition and dependencies
├── mvnw / mvnw.cmd                       # Maven Wrapper for cross-platform execution
├── .env.example                          # Environment variables template
├── schema.sql                            # PostgreSQL DDL schema definition
└── src/
    ├── main/
    │   ├── java/com/spendcheck/
    │   │   ├── SpendCheckApplication.java   # Application entry point
    │   │   ├── config/                      # Configuration layer
    │   │   │   ├── CorsConfig.java          # Cross-Origin Resource Sharing policy
    │   │   │   ├── JwtProperties.java       # JWT properties configuration bean
    │   │   │   └── SecurityConfig.java      # Spring Security 6 filter chain
    │   │   ├── controller/                  # REST Controllers layer
    │   │   │   └── HealthController.java    # /api/v1/health status endpoint
    │   │   ├── dto/                         # Data Transfer Objects
    │   │   │   ├── ApiResponse.java         # Standard API JSON response wrapper
    │   │   │   └── HealthResponse.java      # Health check payload
    │   │   ├── model/                       # Domain Entities layer
    │   │   │   ├── Category.java            # Expense Category enum (Travel, Food, etc.)
    │   │   │   └── Expense.java             # JPA Expense entity
    │   │   ├── repository/                  # Spring Data JPA Repositories
    │   │   │   └── ExpenseRepository.java   # Expense database access queries
    │   │   └── service/                     # Business Logic Services
    │   │       └── HealthService.java       # Health and DB connectivity checker
    │   └── resources/
    │       └── application.properties       # Main application configuration
    └── test/
        ├── java/com/spendcheck/
        │   ├── SpendCheckApplicationTests.java  # Spring context boot test
        │   └── controller/
        │       └── HealthControllerTest.java    # Health API controller slice test
        └── resources/
            └── application.properties           # Isolated in-memory H2 test config
```

---

## ⚙️ Environment Variables

The application reads its credentials and secrets from environment variables with sensible defaults:

| Variable | Description | Default Value |
|---|---|---|
| `PORT` | HTTP server port | `8080` |
| `DB_URL` | PostgreSQL JDBC connection URL | `jdbc:postgresql://localhost:5432/spendcheck` |
| `DB_USERNAME` | PostgreSQL database user | `postgres` |
| `DB_PASSWORD` | PostgreSQL database password | `postgres` |
| `JWT_SECRET` | 256-bit+ secret key for JWT signing | *(Pre-configured 256-bit dev key)* |
| `JWT_EXPIRATION_MS` | JWT expiration duration in ms | `86400000` (24 hours) |

### Setting Environment Variables

#### Option 1: Copy `.env.example`
```bash
cp .env.example .env
```
And adjust your password or credentials.

#### Option 2: PowerShell
```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/spendcheck"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_postgres_password"
$env:JWT_SECRET="your_custom_jwt_secret_key_at_least_256_bits_long"
```

#### Option 3: Command Prompt (cmd)
```cmd
set DB_URL=jdbc:postgresql://localhost:5432/spendcheck
set DB_USERNAME=postgres
set DB_PASSWORD=your_postgres_password
set JWT_SECRET=your_custom_jwt_secret_key_at_least_256_bits_long
```

---

## 🐘 PostgreSQL Database Setup

1. Open `psql` or pgAdmin and create the database:
   ```sql
   CREATE DATABASE spendcheck;
   ```

2. Tables will automatically be created and maintained by Hibernate on startup (`spring.jpa.hibernate.ddl-auto=update`).
   Alternatively, you can manually run `schema.sql` to verify the schema.

---

## 🚀 Running the Backend

From the `/backend` folder:

### Using Maven Wrapper
```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

### Or using installed Maven
```bash
mvn spring-boot:run
```

### Or build and run the executable JAR
```bash
mvn clean package
java -jar target/spendcheck-backend-0.0.1-SNAPSHOT.jar
```

---

## 🩺 Verifying Health Endpoint

Once the backend is running, test the health check endpoint:

```bash
curl http://localhost:8080/api/v1/health
```

Expected response:
```json
{
  "success": true,
  "message": "SpendCheck API is running",
  "data": {
    "status": "UP",
    "service": "SpendCheck Backend API",
    "database": "PostgreSQL 17.x (Connected)",
    "javaVersion": "17.x / 24.x",
    "timestamp": "2026-11-07T11:00:00"
  },
  "timestamp": "2026-11-07T11:00:00"
}
```

---

## 🧪 Running Tests

Automated tests use an isolated in-memory H2 database, requiring no live PostgreSQL instance to run:

```bash
mvn test
```

