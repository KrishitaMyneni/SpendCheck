# SpendCheck - Full-Stack Personal Finance Dashboard

SpendCheck is a personal finance dashboard that lets you:
- Log daily expenses
- Convert currencies using real-time exchange rates
- Group expenses by category
- Persist expense records with a robust PostgreSQL and Spring Boot 3 backend

👉 [Click here to view the frontend live site](https://spendcheck-website.netlify.app/)

---

## 📁 Repository Structure

```
Spendcheck/
├── frontend/                     # Frontend client (HTML5, CSS3, Vanilla JavaScript)
│   ├── index.html                # Main daily expenses dashboard
│   ├── currency.html             # Real-time currency converter
│   ├── grouping.html             # Category grouping visualization
│   ├── mainpage.css              # Frontend styles
│   └── mainpage.js               # Frontend dashboard logic
├── backend/                      # Spring Boot 3 Backend API (Java 17, Maven)
│   ├── pom.xml                   # Maven dependencies and build setup
│   ├── schema.sql                # PostgreSQL table schema and indexes
│   ├── .env.example              # Backend environment variable template
│   ├── src/main/java/com/spendcheck/
│   │   ├── config/               # Security, CORS & JWT configurations
│   │   ├── controller/           # REST API Controllers (Health, etc.)
│   │   ├── dto/                  # Request / Response DTOs
│   │   ├── model/                # JPA Entities (Expense, Category)
│   │   ├── repository/           # Spring Data JPA Repositories
│   │   └── service/              # Business logic & Database connectivity
│   └── src/main/resources/
│       └── application.properties# Config with environment variable support
├── .env.example                  # Root environment variable template
└── .gitignore                    # Git exclusions
```

---

## 🛠️ Tech Stack

### Frontend (`/frontend`)
- HTML5, CSS3, Modern JavaScript (ES6+)
- ExchangeRate API for currency conversions

### Backend (`/backend`)
- **Java**: 17
- **Framework**: Spring Boot 3 (3.3.5)
- **REST**: Spring Web
- **Persistence**: Spring Data JPA & Hibernate
- **Database**: PostgreSQL
- **Security**: Spring Security 6 & JJWT (JSON Web Tokens)
- **Validation**: Jakarta Bean Validation (`spring-boot-starter-validation`)
- **Build Tool**: Apache Maven with Maven Wrapper (`mvnw`)

---

## 🚀 Getting Started

### 1. Database Setup (PostgreSQL)
Ensure PostgreSQL is running locally on port `5432`:
```sql
CREATE DATABASE spendcheck;
```

### 2. Configure Environment Variables
Copy `.env.example` to `.env`:
```bash
cp .env.example .env
```
Default connection values:
- `DB_URL`: `jdbc:postgresql://localhost:5432/spendcheck`
- `DB_USERNAME`: `postgres`
- `DB_PASSWORD`: `postgres`
- `JWT_SECRET`: `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970`

### 3. Run the Backend
```bash
cd backend
./mvnw spring-boot:run
```
On Windows:
```cmd
cd backend
mvnw.cmd spring-boot:run
```

Test the health status:
```bash
curl http://localhost:8080/api/v1/health
```

### 4. Run the Frontend
Open `frontend/index.html` directly in your browser, or serve it using VS Code Live Server or any static HTTP server:
```bash
cd frontend
# e.g., using Python:
python -m http.server 3000
```
Then navigate to `http://localhost:3000`.

---

## 🙋‍♀️ Authors
- Krishita Myneni
- Mallepudi Lalitha mahisri
