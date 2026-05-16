# FitLife Gym Management API

Spring Boot 3.2 REST API for the Angular gym management frontend.

## Prerequisites

- Java 17+
- Maven 3.9+
- MySQL 8+

## Database setup

### Option A — PowerShell script (recommended after MySQL install)

```powershell
cd C:\Users\Juhi Wankhede\Gym-Management-Backend\scripts
.\setup-database.ps1
# If your MySQL root password is not "root":
.\setup-database.ps1 -Password "YourPassword"
# Recreate from scratch:
.\setup-database.ps1 -Password "YourPassword" -Fresh
```

This creates **`gym_management`** with all tables and demo data.

### Option B — MySQL Workbench

1. Open **MySQL Workbench** → connect as `root`.
2. Run `scripts/00_create_database.sql`.
3. Run `src/main/resources/db/migration/V1__schema.sql` on `gym_management`.
4. Run `src/main/resources/db/migration/V2__seed_data.sql`.

### Option C — Start Spring Boot only

Ensure `application.yml` credentials match MySQL, then `mvn spring-boot:run`. Flyway creates and seeds the database automatically.

### Credentials

Default in `application.yml`: user **`root`**, password **`root`**.  
If you set a different password during MySQL Community Setup, copy `application-local.yml.example` to `application-local.yml` and update the password.

## Run the API (connects to MySQL `gym_management`)

1. Ensure MySQL is running and the database exists (see **Database setup**).
2. Match `spring.datasource.username` / `password` in `application.yml` (or `application-local.yml`).
3. Start the API:

```bash
mvn spring-boot:run
```

On success you should see logs like:

```
MySQL connected: catalog=gym_management, ...
Database ready — organizations: 1, members: 4
```

- API base URL: `http://localhost:8080/api/v1`
- Health check: `http://localhost:8080/actuator/health`

## Default login

| Email | Password | Roles |
|-------|----------|-------|
| `admin@gym.com` | `password` | SUPER_ADMIN, BRANCH_MANAGER |
| `reception@gym.com` | `password` | RECEPTIONIST, BRANCH_MANAGER |

## Connect Angular frontend (integrated)

`Gym-Management/src/environments/environment.ts` is already set to:

```typescript
apiUrl: 'http://localhost:8080/api/v1',
useMockApi: false,
```

1. Start **backend** first (`mvn spring-boot:run` on port 8080).
2. Start **frontend**: `npm start` in `Gym-Management` (port 4200).
3. Login at http://localhost:4200 with `reception@gym.com` / `password`.

CORS allows `http://localhost:4200`. The frontend sends `Authorization: Bearer …` and `X-Branch-Id` automatically.

The frontend sends `Authorization: Bearer <token>` and optional `X-Branch-Id` for branch-scoped operations.

## API overview

| Method | Path | Description |
|--------|------|-------------|
| POST | `/auth/login` | Login |
| GET | `/auth/me` | Current user |
| GET | `/members` | Paginated member list |
| GET | `/members/lookup?q=` | Quick search |
| GET/POST/PUT | `/members`, `/members/{id}` | CRUD |
| GET | `/plans?active=` | Membership plans |
| GET | `/invoices`, `/payments` | Billing |
| POST | `/payments` | Record payment |
| GET | `/attendance`, `/attendance/live` | Attendance |
| POST | `/attendance/check-in`, `/attendance/check-out` | Check-in / check-out |
| POST | `/attendance/check-in` | Check-in (requires active subscription) |
| GET | `/dashboard/summary?branchId=` | Dashboard stats |
| GET | `/reports/expiring?branchId=&days=` | Expiring memberships |
| GET | `/branches` | Branches for current user |
| GET | `/staff` | List staff (SUPER_ADMIN, BRANCH_MANAGER) |
| POST | `/staff` | Create staff with roles + branches |

## Configuration

Key settings in `application.yml`:

- `spring.datasource.*` — MySQL connection
- `app.jwt.secret` — change in production
- `app.cors.allowed-origins` — frontend origin(s)
# gym-management-backend
gym management backend
