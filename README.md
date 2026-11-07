# SpendCheck - Full-Stack Personal Finance Dashboard

SpendCheck is a modern, full-stack personal finance and expense tracking application. It allows users to record daily expenses, categorize transactions, view spending breakdowns and analytics, and convert currencies in real-time with live exchange rate caching.

👉 **Live Frontend:** [https://spendcheck-website.netlify.app/](https://spendcheck-website.netlify.app/)

---

## 🏗️ Architecture

```
┌─────────────────────────────────┐
│       Frontend (Netlify)        │
│  HTML5 / CSS3 / JavaScript SPA  │
│  Routes: /, /currency, /grouping│
└────────────────┬────────────────┘
                 │ HTTPS (Bearer JWT)
                 ▼
┌─────────────────────────────────┐
│     Backend (Render Cloud)      │
│  Spring Boot 3 (Java 17, Docker)│
│  Spring Security, JPA, JJWT     │
└────────────────┬────────────────┘
                 │ JDBC (SSL)
                 ▼
┌─────────────────────────────────┐
│      Database (Neon Cloud)      │
│   Managed Serverless PostgreSQL │
│   Pooled connections (Hikari)   │
└─────────────────────────────────┘
```

- **Frontend Client (Netlify)**: Static single-page application served directly from Netlify CDN (`publish = "frontend"` defined in `netlify.toml`). Includes clean routing for `/currency` and `/grouping`, client-side JWT authorization, and dynamic dashboard rendering.
- **Backend API (Render)**: Containerized Spring Boot 3 REST API deployed on Render with a lightweight multi-stage Dockerfile. Dynamically adapts to the cloud environment's `PORT` variable.
- **Database (Neon PostgreSQL)**: Cloud-native serverless PostgreSQL on Neon. Connection pooling is managed via HikariCP configured with 5 connections and 30s timeouts.

> ⏳ **Free-Tier Cold Start Note**: The backend is deployed on Render's free tier. If the service has been idle, it enters sleep mode and may take **30 to 60 seconds** to wake up on the first incoming request. Subsequent requests respond instantly.

---

## 📁 Repository Structure

```
Spendcheck/
├── frontend/                     # Client application (HTML5, CSS3, ES6 JS)
│   ├── config.js                 # API base URL configuration (API_BASE_URL)
│   ├── api.js                    # Authenticated API client & token manager
│   ├── index.html                # Main daily expenses dashboard & log
│   ├── login.html                # User login page
│   ├── signup.html               # User registration page
│   ├── currency.html             # Currency conversion calculator
│   ├── grouping.html             # Spending categories breakdown & summary
│   ├── mainpage.css              # Theme styling
│   └── mainpage.js               # Dashboard DOM logic
├── backend/                      # Spring Boot 3 API (Java 17, Maven)
│   ├── Dockerfile                # Multi-stage production container build
│   ├── pom.xml                   # Maven dependencies and build configuration
│   ├── schema.sql                # PostgreSQL DDL table definitions & indexes
│   ├── .env.example              # Template for backend environment variables
│   ├── src/main/java/com/spendcheck/
│   │   ├── config/               # SecurityConfig, JwtAuthFilter, CorsConfig
│   │   ├── controller/           # Auth, Expense, Currency, Health controllers
│   │   ├── dto/                  # Request / Response Transfer Objects
│   │   ├── exception/            # GlobalExceptionHandler & custom exceptions
│   │   ├── model/                # User & Expense JPA entities, Category enum
│   │   ├── repository/           # UserRepository & ExpenseRepository
│   │   └── service/              # AuthService, ExpenseService, CurrencyService
│   └── src/main/resources/
│       └── application.properties# Config reading only from environment variables
├── netlify.toml                  # Netlify deployment configuration & URL rewrites
├── .env.example                  # Root environment template
└── .gitignore                    # Git exclusions
```

---

## ⚙️ Environment Variables

All database credentials, JWT secrets, and CORS origins are loaded **strictly** from environment variables. Real secrets must never be committed to source control.

| Variable | Description | Example / Notes |
| :--- | :--- | :--- |
| `PORT` | Backend server HTTP port | `8080` (or injected by Render) |
| `DB_URL` | JDBC URL for PostgreSQL / Neon | `jdbc:postgresql://<neon-host>/spendcheck?sslmode=require` |
| `DB_USERNAME` | Database username | Provided by Neon console |
| `DB_PASSWORD` | Database password | Provided by Neon console |
| `JWT_SECRET` | Secret key for signing HMAC-SHA256 tokens | Minimum 256 bits (32+ chars / 64 hex characters) |
| `JWT_EXPIRATION_MS` | JWT token validity in milliseconds | `86400000` (24 hours) |
| `CORS_ORIGINS` | Comma-separated list of allowed frontend origins | `http://localhost:3000,https://spendcheck-website.netlify.app` |

---

## 📡 REST API Endpoints

All protected endpoints require an `Authorization: Bearer <token>` header.

### 1. Authentication (`/api/v1/auth`)
| Method | Endpoint | Access | Description | Status Code |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/signup` | Public | Register a new user (`name`, `email`, `password`) | `201 Created` / `400` / `409 Conflict` |
| `POST` | `/api/v1/auth/login` | Public | Authenticate and retrieve JWT token | `200 OK` / `401 Unauthorized` |

### 2. Expenses CRUD (`/api/v1/expenses`)
| Method | Endpoint | Access | Description | Status Code |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/expenses` | Protected | List user expenses (filters: `category`, `startDate`, `endDate`) | `200 OK` |
| `POST` | `/api/v1/expenses` | Protected | Create an expense (`title`, `amount`, `category`, `expenseDate`, `notes`) | `201 Created` / `400` |
| `GET` | `/api/v1/expenses/{id}` | Protected | Get an expense by ID (user isolated) | `200 OK` / `404 Not Found` |
| `PUT` | `/api/v1/expenses/{id}` | Protected | Update an existing expense | `200 OK` / `404 Not Found` |
| `DELETE` | `/api/v1/expenses/{id}` | Protected | Delete an expense | `204 No Content` / `404 Not Found` |

### 3. Analytics & Summaries (`/api/v1/expenses/summary`)
| Method | Endpoint | Access | Description | Status Code |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/expenses/summary/categories` | Protected | Aggregate total spend and count per category | `200 OK` |
| `GET` | `/api/v1/expenses/summary/monthly` | Protected | Monthly spend breakdown (optional: `?year=2026&month=11`) | `200 OK` |

### 4. Currency Conversion (`/api/v1/currency`)
| Method | Endpoint | Access | Description | Status Code |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/currency/convert` | Protected | Convert currency with 1-hr in-memory cache (`?from=USD&to=EUR&amount=100`) | `200 OK` / `400` / `502` |

### 5. System Health (`/api/v1/health`)
| Method | Endpoint | Access | Description | Status Code |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/health` | Public | Application and database connectivity status | `200 OK` / `503 Service Unavailable` |

---

## 🚀 Local Development Setup

### 1. Prerequisites
- **Java 17+** JDK
- **Maven 3.8+** (or use included `./mvnw`)
- **PostgreSQL** (local instance or free cloud database on [Neon.tech](https://neon.tech))
- **Docker** (optional, for containerized run)

### 2. Configure Environment
Create a `.env` file or export the required environment variables:
```bash
export DB_URL="jdbc:postgresql://localhost:5432/spendcheck"
export DB_USERNAME="postgres"
export DB_PASSWORD="your-db-password"
export JWT_SECRET="your-secure-random-secret-key-at-least-256-bits-long"
export CORS_ORIGINS="http://localhost:3000,http://127.0.0.1:3000,http://localhost:5500"
```

### 3. Run Backend (Local Maven)
```bash
cd backend
./mvnw clean spring-boot:run
```
*(On Windows cmd/powershell: `.\mvnw.cmd clean spring-boot:run`)*

### 4. Run Backend with Docker
```bash
cd backend
docker build -t spendcheck-backend .
docker run -p 8080:8080 \
  -e DB_URL="jdbc:postgresql://<neon-host>/spendcheck?sslmode=require" \
  -e DB_USERNAME="<username>" \
  -e DB_PASSWORD="<password>" \
  -e JWT_SECRET="<jwt-secret>" \
  -e CORS_ORIGINS="http://localhost:3000" \
  spendcheck-backend
```

### 5. Run Frontend
Open `frontend/` with any static web server:
```bash
cd frontend
python -m http.server 3000
```
Then visit `http://localhost:3000/login.html` in your browser. Configure the backend URL in `frontend/config.js` if deploying to production.

---

## 🧪 Running Automated Tests

Tests execute against an isolated in-memory H2 database and do not connect to external networks.
```bash
mvn -f backend/pom.xml test
```
The test suite covers:
- User signup (validation, hashing, duplicate email detection)
- User authentication & JWT generation
- Protected route unauthorized rejection (401 JSON)
- Expense CRUD operations
- Multi-user data isolation (ensuring users cannot see or modify other users' expenses)
- Category & monthly JPQL aggregations
- Currency conversion caching and fallback logic

---

## 👥 Authors
- Krishita Myneni
- Mallepudi Lalitha Mahisri
