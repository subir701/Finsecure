# finSecure — Finance Dashboard Backend

A Spring Boot 4 REST API for managing personal financial records with JWT authentication and role-based access control.

---

## Tech Stack

- Java 21 with Virtual Threads
- Spring Boot 4.0 + Spring Security 6
- Spring Data JPA + Hibernate 7
- PostgreSQL
- JJWT 0.12.x
- SpringDoc OpenAPI (Swagger UI)
- Lombok

---

## Setup

### 1. Create the database
```sql
CREATE DATABASE finsecure;
```

### 2. Configure credentials
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/finsecure
spring.datasource.username=postgres
spring.datasource.password=your_password
jwt.secret=finSecure_super_secret_key_change_in_production_32chars
```

### 3. Run
```bash
./gradlew bootRun -x test
```

Server starts at `http://localhost:8080`.
Swagger UI available at `http://localhost:8080/swagger-ui.html`.
Tables are auto-created by Hibernate (`ddl-auto=update`).

---

## Role Matrix

| Action                        | VIEWER | ANALYST | ADMIN |
|-------------------------------|--------|---------|-------|
| Register / Login              | ✅     | ✅      | ✅    |
| View dashboard summary        | ✅     | ✅      | ✅    |
| View own records              | ✅     | ✅      | ✅    |
| Filter / search records       | ✅     | ✅      | ✅    |
| View all users' records       | ✗      | ✗       | ✅    |
| Create records                | ✗      | ✗       | ✅    |
| Update records                | ✗      | ✗       | ✅    |
| Delete records                | ✗      | ✗       | ✅    |
| Manage users (role, status)   | ✗      | ✗       | ✅    |

> New registrations default to **VIEWER**. An ADMIN promotes roles via `PATCH /api/admin/users/{id}/role`.

---

## Assumptions

1. **VIEWER** can view their own dashboard summary — the assignment explicitly states "Viewer: Can only view dashboard data."
2. **ANALYST** can view records and access insights but cannot create, update, or delete records.
3. **ADMIN** has full access to all records across all users, and manages user accounts.
4. New users default to VIEWER role on registration for safety.
5. `transactionDate` is the user-provided date of the transaction (when it actually happened). `createdAt` is the system audit timestamp (when the record was inserted) and is never user-controlled.
6. `transactionDate` defaults to today if not provided in the request.
7. Monthly trends cover the last 6 months. The `TO_CHAR` function used is PostgreSQL-specific.
8. Soft delete is not implemented — deletion is permanent and immediate.
9. The `DaoAuthenticationProvider` bean warning from Spring Security is expected and suppressed via logging config — our manual wiring is intentional.

---

## API Endpoints

### Auth — no token required

| Method | Endpoint             | Body                          | Response                        |
|--------|----------------------|-------------------------------|---------------------------------|
| POST   | `/api/auth/register` | `{username, email, password}` | `201` success message           |
| POST   | `/api/auth/login`    | `{email, password}`           | `200` `{token, email, role}`    |

### Records — Bearer token required

| Method | Endpoint            | Access              | Notes                                        |
|--------|---------------------|---------------------|----------------------------------------------|
| POST   | `/api/records`      | ADMIN               | Create a new record                          |
| GET    | `/api/records`      | VIEWER, ANALYST, ADMIN | List records with filters + pagination    |
| GET    | `/api/records/{id}` | VIEWER, ANALYST, ADMIN | Single record (own only, or any for ADMIN)|
| PUT    | `/api/records/{id}` | ADMIN               | Update a record                              |
| DELETE | `/api/records/{id}` | ADMIN               | Delete a record                              |

**Record request body:**
```json
{
  "amount": 1500.00,
  "category": "FOOD",
  "recordType": "EXPENSE",
  "note": "Grocery shopping",
  "transactionDate": "2025-04-01"
}
```

**Query params for GET /api/records:**
```
category       = FOOD | RENT | SALARY | ENTERTAINMENT | UTILITIES | INVESTMENT | OTHER
recordType     = INCOME | EXPENSE
from           = 2025-01-01T00:00:00   (ISO 8601)
to             = 2025-12-31T23:59:59
page           = 0   (default)
size           = 10  (default)
```

### Dashboard — Bearer token required

| Method | Endpoint                 | Access                  | Returns                                           |
|--------|--------------------------|-------------------------|---------------------------------------------------|
| GET    | `/api/dashboard/summary` | VIEWER, ANALYST, ADMIN  | totals, net balance, category map, monthly trends |

**Sample response:**
```json
{
  "totalIncome": 50000.00,
  "totalExpenses": 23500.00,
  "netBalance": 26500.00,
  "expensesByCategory": {
    "FOOD": 8000.00,
    "RENT": 12000.00,
    "UTILITIES": 3500.00
  },
  "incomeByCategory": {
    "SALARY": 50000.00
  },
  "monthlyTrends": [
    { "month": "2025-01", "income": 50000.00, "expenses": 23500.00, "net": 26500.00 }
  ],
  "recentActivity": [...]
}
```

### Admin — ADMIN role only

| Method | Endpoint                       | Notes                              |
|--------|--------------------------------|------------------------------------|
| GET    | `/api/admin/users`             | List all users. `?isActive=true/false` to filter |
| GET    | `/api/admin/users/{id}`        | Get user by ID                     |
| PATCH  | `/api/admin/users/{id}/status` | `?isActive=true/false`             |
| PATCH  | `/api/admin/users/{id}/role`   | `?role=VIEWER/ANALYST/ADMIN`       |
| DELETE | `/api/admin/users/{id}`        | Permanently delete user            |

---

## Error Responses

All errors return consistent JSON:
```json
{
  "message": "Record not found with id: ...",
  "status": 404,
  "timestamp": "2025-04-03T10:30:00"
}
```

Validation errors include per-field breakdown:
```json
{
  "message": "Validation failed",
  "status": 400,
  "errors": {
    "amount": "Amount must be greater than zero",
    "transactionDate": "Transaction date cannot be in the future"
  },
  "timestamp": "2025-04-03T10:30:00"
}
```

---

## Project Structure

```
src/main/java/com/finSecure/
├── FinsecureApplication.java         ← Virtual threads + OpenAPI config
├── controller/
│   ├── AuthController.java           ← POST /api/auth/register|login
│   ├── RecordController.java         ← CRUD + filter on /api/records
│   ├── DashboardController.java      ← GET /api/dashboard/summary
│   └── AdminController.java          ← /api/admin/users/**
├── service/
│   ├── AuthService.java
│   ├── RecordService.java
│   ├── DashboardService.java
│   └── AdminService.java
├── repository/
│   ├── UserRepository.java           ← Custom JPQL for status filter + update
│   └── RecordRepository.java         ← Filter queries + dashboard aggregations
├── entity/
│   ├── User.java
│   ├── Record.java                   ← transactionDate + createdAt (audit)
│   ├── Role.java                     ← VIEWER | ANALYST | ADMIN
│   ├── Category.java
│   └── RecordType.java               ← INCOME | EXPENSE
├── dto/
│   ├── request/
│   │   ├── UserRegisterRequest.java
│   │   ├── LoginRequest.java
│   │   └── RecordRequest.java        ← includes transactionDate
│   └── response/
│       ├── LoginResponse.java
│       ├── RecordResponse.java       ← includes transactionDate
│       ├── UserResponse.java         ← safe projection, no password/recordList
│       └── DashboardSummaryResponse.java
├── security/
│   ├── JwtUtil.java
│   ├── JwtAuthFilter.java
│   ├── CustomUserDetailsService.java
│   └── SecurityConfig.java
├── filter/
│   └── RequestLoggingFilter.java     ← logs all requests with timing + MDC requestId
└── exception/
    ├── ApiException.java
    └── GlobalExceptionHandler.java
```
