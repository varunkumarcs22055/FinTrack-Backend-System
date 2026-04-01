# FinTrack — Finance Data Processing & Access Control Backend

A Spring Boot backend for managing financial records with role-based access control.  
Built for the **Zorvyn Backend Developer Intern** assessment.

---

## 🏗️ Architecture

```
Controller Layer  →  Service Layer  →  Repository Layer  →  Database
   (REST API)        (Business Logic)    (Data Access)       (H2/PostgreSQL)
```

**Design Pattern:** Layered architecture with clear separation of concerns.

| Layer | Responsibility |
|---|---|
| **Controller** | Request/response handling, validation, Swagger docs |
| **Service** | Business logic, DTO mapping, access control enforcement |
| **Repository** | Data access via Spring Data JPA |
| **Config** | Security, JWT, Swagger, data seeding |
| **Exception** | Global error handling with consistent error responses |

### Key Design Decisions

| Decision | Why |
|---|---|
| **DTOs everywhere** | Never expose JPA entities directly — prevents lazy-loading issues and data leaks (e.g., password hashes) |
| **Role as Enum** | Only 3 fixed roles — a join table adds complexity without value |
| **BigDecimal for money** | `double`/`float` cause rounding errors in financial calculations |
| **Unidirectional ManyToOne** | FinancialRecord → User, avoids circular serialization |
| **H2 default** | Zero-install local testing; PostgreSQL available via profile |
| **JWT in Authorization header** | Stateless, scalable, standard REST practice |
| **@PreAuthorize** | Method-level security with Spring's built-in RBAC |

---

## 🚀 Quick Start

### Prerequisites
- **Java 17+**
- **Maven 3.8+**

### Run Locally
```bash
cd finance-backend
mvn spring-boot:run
```

The app starts at `http://localhost:8080` with:
- **Frontend:** `http://localhost:8080` (static HTML dashboard)
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **H2 Console:** `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:financedb`)

### Default Users (auto-seeded)

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `analyst` | `analyst123` | ANALYST |
| `viewer` | `viewer123` | VIEWER |

---

## 👥 Role Permissions

| Action | ADMIN | ANALYST | VIEWER |
|---|:---:|:---:|:---:|
| Register / Login | ✅ | ✅ | ✅ |
| View Dashboard | ✅ | ✅ | ✅ |
| View Records | ✅ | ✅ | ❌ |
| Create/Edit/Delete Records | ✅ | ❌ | ❌ |
| Manage Users | ✅ | ❌ | ❌ |

---

## 📡 API Reference

### Authentication
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | Register new user (VIEWER role) | Public |
| POST | `/api/auth/login` | Login and get JWT | Public |

### User Management (ADMIN only)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/users` | List all users |
| GET | `/api/users/{id}` | Get user by ID |
| PUT | `/api/users/{id}/role` | Update user role |
| PUT | `/api/users/{id}/status` | Toggle active/inactive |
| DELETE | `/api/users/{id}` | Delete user |

### Financial Records
| Method | Endpoint | Description | Roles |
|---|---|---|---|
| POST | `/api/records` | Create record | ADMIN |
| GET | `/api/records` | List records (paginated + filterable) | ADMIN, ANALYST |
| GET | `/api/records/{id}` | Get record by ID | ADMIN, ANALYST |
| PUT | `/api/records/{id}` | Update record | ADMIN |
| DELETE | `/api/records/{id}` | Delete record | ADMIN |
| GET | `/api/records/categories` | List distinct categories | ADMIN, ANALYST |

**Filtering Parameters** (GET /api/records):
- `type` — INCOME or EXPENSE
- `category` — category name
- `startDate` — from date (yyyy-MM-dd)
- `endDate` — to date (yyyy-MM-dd)
- `page` — page number (default: 0)
- `size` — page size (default: 20)

### Dashboard (All authenticated users)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/dashboard/summary` | Total income, expenses, net balance |
| GET | `/api/dashboard/category-totals` | Category-wise breakdown |
| GET | `/api/dashboard/monthly-trends` | Monthly income/expense trends |
| GET | `/api/dashboard/recent` | Last 10 transactions |

---

## 🔐 Authentication Flow

```
1. POST /api/auth/login  →  { username, password }
2. Server returns  →  { token, username, role }
3. Client sends token in header  →  Authorization: Bearer <token>
4. Server validates JWT + checks role permissions
```

---

## 🛡️ Error Handling

All errors return a consistent JSON structure:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": ["Amount must be positive", "Category is required"],
  "timestamp": "2026-04-01T20:30:00"
}
```

| Status | Meaning |
|---|---|
| 400 | Validation error or bad input |
| 401 | Missing/invalid JWT or bad credentials |
| 403 | Insufficient role permissions |
| 404 | Resource not found |
| 409 | Duplicate resource (e.g., username exists) |
| 500 | Unexpected server error |

---

## 📊 Data Model

```
┌──────────────────┐       ┌──────────────────────┐
│      users       │       │  financial_records    │
├──────────────────┤       ├──────────────────────┤
│ id (PK)          │       │ id (PK)              │
│ username (UK)    │◄──────│ created_by (FK)      │
│ email (UK)       │       │ amount (BigDecimal)  │
│ password (hash)  │       │ type (INCOME/EXPENSE)│
│ role (ENUM)      │       │ category             │
│ active (boolean) │       │ date                 │
│ created_at       │       │ description          │
└──────────────────┘       │ created_at           │
                           └──────────────────────┘
```

---

## 📁 Project Structure

```
com.finance.backend/
├── config/           # Security, JWT, Swagger, DataSeeder
├── controller/       # REST endpoints (thin layer)
├── service/          # Business logic
├── repository/       # Spring Data JPA interfaces
├── entity/           # JPA entities + enums
├── dto/              # Request/response objects (auth, user, finance, dashboard)
└── exception/        # Global error handler + custom exceptions
```

---

## 🧪 Testing with Swagger

1. Open `http://localhost:8080/swagger-ui.html`
2. Use **POST /api/auth/login** with `admin / admin123`
3. Copy the `token` from the response
4. Click **Authorize** (top right) → paste `Bearer <token>`
5. All secured endpoints are now accessible

---

## 📝 Assumptions

1. Each user has exactly one role (no multi-role support)
2. Financial records are globally visible to authorized roles (not per-user isolated)
3. Categories are free-text strings for flexibility (not a fixed enum)
4. JWT tokens expire after 24 hours
5. New users register as VIEWER by default — only ADMIN can promote roles
6. H2 in-memory database is used by default (data resets on restart) — suitable for assessment
7. The `created_by` field tracks who created a record but doesn't restrict read access

---

## ⚙️ Tech Stack

| Technology | Purpose |
|---|---|
| Java 17 | Language |
| Spring Boot 3.2.5 | Framework |
| Spring Security | Authentication & authorization |
| Spring Data JPA | Data access |
| H2 Database | Development database |
| jjwt 0.12.5 | JWT token handling |
| Lombok | Boilerplate reduction |
| springdoc-openapi 2.5 | Swagger UI |
| BCrypt | Password hashing |

---

## 📄 License

This project is built for the Zorvyn FinTech internship assessment.

**Author:** Varun Kumar Thakur  
**Email:** varunkumarthakur021@gmail.com
