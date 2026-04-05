# finSecure — Finance Dashboard Backend

A Spring Boot 4 REST API for managing financial records with JWT authentication, role-based access control, virtual threads, and structured request logging.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (Virtual Threads via Tomcat) |
| Framework | Spring Boot 4.0 |
| Security | Spring Security 6 + Stateless JWT (JJWT 0.12.x) |
| Persistence | Spring Data JPA + Hibernate 7 + PostgreSQL |
| Query Strategy | JPQL with `cast(:param as localdate)` for nullable date params |
| API Docs | SpringDoc OpenAPI — Swagger UI |
| Utilities | Lombok, SLF4J + MDC request-ID logging |

---

## Project Structure

```
src/main/java/com/finSecure/
├── FinsecureApplication.java              ← Entry point + virtual thread config
├── config/
│   └── SecurityConfig.java               ← Spring Security filter chain, JWT wiring
├── controller/
│   ├── AuthController.java               ← POST /api/auth/register|login
│   ├── RecordController.java             ← CRUD + filter on /api/records
│   ├── DashboardController.java          ← /api/dashboard/**
│   └── AdminController.java              ← /api/admin/users/**
├── service/
│   ├── AuthService.java / AuthServiceImpl.java
│   ├── RecordService.java / RecordServiceImpl.java
│   ├── DashboardService.java / DashboardServiceImpl.java
│   ├── AdminService.java / AdminServiceImpl.java
│   └── CustomUserDetailsService.java     ← Spring Security UserDetailsService
├── repository/
│   ├── RecordRepository.java             ← JPQL filters + dashboard aggregations
│   └── UserRepository.java              ← Status filter + modifying update
├── entity/
│   ├── User.java
│   ├── Record.java                       ← transactionDate (user-set) + createdAt (audit)
│   ├── Role.java                         ← VIEWER | ANALYST | ADMIN
│   ├── Category.java                     ← FOOD | RENT | SALARY | ENTERTAINMENT | UTILITIES | INVESTMENT | OTHER
│   └── RecordType.java                   ← INCOME | EXPENSE
├── dto/
│   ├── request/
│   │   ├── UserRegisterRequest.java
│   │   ├── LoginRequest.java
│   │   └── RecordRequest.java
│   └── response/
│       ├── LoginResponse.java
│       ├── RecordResponse.java           ← includes transactionDate + createdAt
│       ├── UserResponse.java             ← safe projection, excludes password + recordList
│       ├── BasicDashboardSummaryResponse.java  ← VIEWER only: totals + recent activity
│       ├── DashboardSummaryResponse.java ← full summary with trends + categories
│       ├── TotalsResponse.java           ← income, expenses, net
│       ├── CategorySummaryResponse.java  ← expensesByCategory + incomeByCategory
│       └── TrendResponse.java           ← monthlyTrends list
├── security/
│   ├── JwtUtil.java                      ← token generation + validation
│   └── JwtAuthFilter.java               ← OncePerRequestFilter, sets SecurityContext
├── filter/
│   └── RequestLoggingFilter.java        ← logs method, URI, status, duration + MDC requestId
└── exception/
    ├── ApiException.java                 ← typed runtime exception with HttpStatus
    ├── ErrorResponse.java               ← error envelope record
    └── GlobalExceptionHandler.java      ← @RestControllerAdvice, consistent error shape
```

---

## Setup

### 1. Create the database
```sql
CREATE DATABASE finsecure;
```

### 2. Configure `src/main/resources/application.properties`
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/finsecure
spring.datasource.username=postgres
spring.datasource.password=your_password
jwt.secret=finSecure_super_secret_key_change_in_production_32chars
jwt.expiration-ms=86400000
```

### 3. Run
```bash
./gradlew bootRun -x test
```

- API base: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Tables auto-created by Hibernate (`ddl-auto=update`)

### 4. Authenticate in Swagger
1. Call `POST /api/auth/login` → copy the `token` field
2. Click **Authorize 🔒** at the top of Swagger UI
3. Paste the token (no `Bearer ` prefix — Swagger adds it)
4. All subsequent requests will include the Authorization header

---

## Role Model

| Role | Purpose | Access Level |
|---|---|---|
| **VIEWER** | Stakeholder who monitors financial health | Global dashboard summary (totals + recent activity) only. No record access. |
| **ANALYST** | Data analyst who reviews records and trends | Full read on all records. No mutations. |
| **ADMIN** | System administrator | Full access — record CRUD, user management, platform-wide analytics. |

> New users default to **VIEWER** on registration. Admin promotes roles via `PATCH /api/admin/users/{id}/role`.

---

## Access Control Matrix

### Records (`/api/records`)

| Action | VIEWER | ANALYST | ADMIN |
|---|---|---|---|
| List records | ✗ | ✅ `userId` required | ✅ `userId` optional |
| Get record by ID | ✗ | ✅ | ✅ |
| Create record | ✗ | ✗ | ✅ |
| Update record | ✗ | ✗ | ✅ |
| Delete record | ✗ | ✗ | ✅ |

### Dashboard (`/api/dashboard`)

| Endpoint | VIEWER | ANALYST | ADMIN |
|---|---|---|---|
| `GET /viewer/summary` | ✅ | ✅ | ✅ |
| `GET /my/totals` | ✗ | ✅ | ✅ |
| `GET /my/categories` | ✗ | ✅ | ✅ |
| `GET /my/trends` | ✗ | ✅ | ✅ |
| `GET /my/recent` | ✗ | ✅ | ✅ |
| `GET /global/totals` | ✗ | ✅ | ✅ |
| `GET /global/categories` | ✗ | ✅ | ✅ |
| `GET /global/trends` | ✗ | ✅ | ✅ |
| `GET /global/recent` | ✗ | ✅ | ✅ |
| `GET /global/summary` | ✗ | ✗ | ✅ |

### Admin (`/api/admin`)

| Endpoint | VIEWER | ANALYST | ADMIN |
|---|---|---|---|
| All `/api/admin/**` | ✗ | ✗ | ✅ |

---

## API Reference

### Auth — no token required

| Method | Endpoint | Body |
|---|---|---|
| POST | `/api/auth/register` | `{ username, email, password }` |
| POST | `/api/auth/login` | `{ email, password }` |

**Login response:**
```json
{
  "token": "eyJhbGci...",
  "email": "admin@example.com",
  "role": "ADMIN",
  "message": "Login successful"
}
```

---

### Records — Bearer token required

**POST / PUT body:**
```json
{
  "amount": 18000.00,
  "category": "RENT",
  "recordType": "EXPENSE",
  "note": "April rent payment",
  "transactionDate": "2026-04-01"
}
```

**GET /api/records — query parameters:**

| Param | Type | Required | Notes |
|---|---|---|---|
| `userId` | UUID | ANALYST: yes / ADMIN: no | VIEWER: not allowed |
| `category` | enum | no | FOOD, RENT, SALARY, ENTERTAINMENT, UTILITIES, INVESTMENT, OTHER |
| `recordType` | enum | no | INCOME, EXPENSE |
| `from` | date | no | Format: `2026-04-01` |
| `to` | date | no | Format: `2026-04-30` |
| `page` | int | no | Default: 0 |
| `size` | int | no | Default: 10 |

**Record response:**
```json
{
  "recordId": "3f7a1b2c-...",
  "amount": 18000.00,
  "category": "RENT",
  "recordType": "EXPENSE",
  "note": "April rent payment",
  "transactionDate": "2026-04-01",
  "createdAt": "2026-04-05T10:30:00"
}
```

---

### Dashboard — Bearer token required

#### VIEWER — `GET /api/dashboard/viewer/summary`
Minimal payload — only what a stakeholder needs at a glance:
```json
{
  "totalIncome": 170000.00,
  "totalExpenses": 56700.00,
  "netBalance": 113300.00,
  "recentActivity": [...]
}
```

#### ADMIN — user-scoped (`/my/*`)
Own records of the authenticated user:
```
GET /api/dashboard/my/totals      → { totalIncome, totalExpenses, netBalance }
GET /api/dashboard/my/categories  → { expensesByCategory, incomeByCategory }
GET /api/dashboard/my/trends      → { monthlyTrends: [{ month, income, expenses, net }] }
GET /api/dashboard/my/recent      → [ last 10 records ]
```

#### ANALYST / ADMIN — platform-wide (`/global/*`)
Aggregated across all users — meaningful because Admin creates all records:
```
GET /api/dashboard/global/totals             → { totalIncome, totalExpenses, netBalance }
GET /api/dashboard/global/categories         → { expensesByCategory, incomeByCategory }
GET /api/dashboard/global/trends?months=6    → { monthlyTrends } (default: 6, configurable)
GET /api/dashboard/global/recent             → [ last 10 records across all users ]
```

#### ADMIN only
```
GET /api/dashboard/global/summary  → complete platform overview in one response
```

---

### Admin — ADMIN role only

| Method | Endpoint | Notes |
|---|---|---|
| GET | `/api/admin/users` | All users. Filter: `?isActive=true\|false` |
| GET | `/api/admin/users/{id}` | Single user |
| PATCH | `/api/admin/users/{id}/role?role=ANALYST` | Promote / demote |
| PATCH | `/api/admin/users/{id}/status?isActive=false` | Activate / deactivate |
| DELETE | `/api/admin/users/{id}` | Permanently delete |

**User response (password excluded):**
```json
{
  "userId": "uuid",
  "username": "john",
  "email": "john@example.com",
  "role": "ANALYST",
  "isActive": true,
  "createdAt": "2026-04-01T09:00:00"
}
```

---

## Error Responses

All errors return a consistent envelope:
```json
{
  "message": "Record not found with id: ...",
  "status": 404,
  "timestamp": "2026-04-05T10:30:00"
}
```

Validation errors include per-field detail:
```json
{
  "message": "Validation failed",
  "status": 400,
  "errors": {
    "amount": "Amount must be greater than zero",
    "transactionDate": "Transaction date cannot be in the future"
  },
  "timestamp": "2026-04-05T10:30:00"
}
```

---

## Design Decisions & Assumptions

### 1. Role-based dashboard architecture
Records are created exclusively by Admin. Scoping Viewer or Analyst to their own `userId` would return empty dashboards — defeating the purpose of having a dashboard at all.

- **VIEWER** sees `BasicDashboardSummaryResponse` — totals and recent activity only. No category breakdowns or trends (those are analytical insights, not basic view data).
- **ANALYST** sees platform-wide (`/global/*`) endpoints to support insight analysis.
- **ADMIN** has full access including `GET /global/summary` as a single combined response.

### 2. Two date fields on Record
- `transactionDate` — user-provided date of when the transaction occurred. Used for filtering and trend aggregation.
- `createdAt` — system audit timestamp set on insert via `@PrePersist`. Never user-controlled.

### 3. Analyst requires userId to view records
Analyst hits `GET /api/records?userId=<uuid>` to view a specific user's records. Returns `400` if `userId` is omitted. This enforces intentional data access rather than accidental full dumps.

### 4. JPQL null-safe date filtering
PostgreSQL cannot infer types for untyped null parameters in JPQL. Date params use `cast(:param as localdate)` pattern so null values are handled correctly without `SQLState 42P18` errors.

### 5. UserResponse DTO
The `User` entity has a lazy `recordList` collection and a `password` field. Returning the entity directly causes Jackson serialization failures (`LazyInitializationException`) and exposes password hashes. `UserResponse` is a safe projection that excludes both.

### 6. Virtual threads
All Tomcat request threads run as virtual threads via `TomcatProtocolHandlerCustomizer`. Combined with `spring.threads.virtual.enabled=true`, this removes thread-pool exhaustion under I/O load with no application code changes.

### 7. Request logging with MDC
`RequestLoggingFilter` injects a short `requestId` into SLF4J's MDC at the start of every request. Every log line produced during that request carries the same ID, making distributed tracing trivial without an external tool.

### 8. Soft delete not implemented
User and record deletion is permanent. The `isActive` flag on `User` serves as a logical deactivation without deletion — deactivated users cannot log in but their data is preserved.

---

## Optional Enhancements Implemented

| Enhancement | Implementation |
|---|---|
| JWT Authentication | Stateless Bearer token via JJWT, validated on every request |
| Pagination | `PageResponse<T>` wrapper on `GET /api/records` |
| API Documentation | SpringDoc + `@SecurityScheme` for Authorize button in Swagger UI |
| Virtual Threads | Java 21 via `TomcatProtocolHandlerCustomizer` |
| Structured Logging | SLF4J + MDC `requestId` via `RequestLoggingFilter` |
| Input Validation | `@Valid` on all request bodies, `@PastOrPresent` on `transactionDate` |
| User status management | `isActive` flag with Admin-controlled toggle |
