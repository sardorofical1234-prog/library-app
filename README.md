# Library App — Library Management REST API

![CI](https://github.com/sardorofical1234-prog/library-app/actions/workflows/ci.yml/badge.svg)

Java 21 · Spring Boot 4.1 · Spring Security · JWT · Spring Data JPA · PostgreSQL 17 · Flyway · Docker Compose

A backend portfolio project for managing a small library, memberships, book inventory and borrowing workflows.

## Features

- Member registration and JWT login (15-minute access tokens, HS256)
- BCrypt password hashing and <code>MEMBER</code> / <code>ADMIN</code> role-based authorization
- Book CRUD for administrators; authenticated search and pagination for members
- Borrow and return workflows with inventory consistency checks
- Business rules: 14-day loan period, maximum 3 active loans per member, no duplicate active loan for the same book
- Overdue loan reporting for administrators
- Database row locking to protect stock and member borrowing limits under concurrent requests
- Flyway-managed PostgreSQL schema, Hibernate schema validation
- Centralized API error responses and request validation
- Unit tests plus PostgreSQL-backed integration test in GitHub Actions
- Docker image runs as a non-root user

## Run locally with Docker Compose

Requirements: Docker Engine and Docker Compose.

~~~bash
cp .env.example .env
# Edit .env: use a private random JWT key and a non-default database password.
docker compose up --build -d
docker compose logs -f app
~~~

Generate a JWT signing secret with:

~~~bash
openssl rand -base64 32
~~~

Paste the generated value into <code>.env</code> as <code>JWT_SECRET_BASE64</code>. The committed <code>.env.example</code> key is only a local placeholder and must never be used in a deployed environment.

The API starts at <code>http://localhost:8080</code>; Swagger UI is at <code>http://localhost:8080/swagger-ui.html</code>.

To create the first administrator, set BOTH optional values in <code>.env</code> before starting the app:

~~~dotenv
BOOTSTRAP_ADMIN_FULL_NAME=Library Administrator
BOOTSTRAP_ADMIN_USERNAME=library-admin
BOOTSTRAP_ADMIN_PASSWORD=use-a-long-unique-password
~~~

The password must contain at least 12 characters. No predictable administrator account is created when these variables are absent. Public registration always creates a <code>MEMBER</code>.

Stop the stack with <code>docker compose down</code>. The database volume is retained; use <code>docker compose down -v</code> only when you intentionally want to delete local database data.

## Database migrations

Flyway runs migrations from <code>src/main/resources/db/migration</code> during startup. Hibernate is configured with <code>ddl-auto: validate</code>; it checks the schema but does not create or update tables.

**Existing local database warning:** this repository previously used Hibernate schema auto-update. Before switching an old development database to Flyway, back up any data you need, then reset the local development volume once with <code>docker compose down -v</code>. Do not delete a production database volume to apply this change; production schema adoption requires a reviewed baseline/migration plan.

## API overview

All protected endpoints use:

~~~http
Authorization: Bearer <access-token>
~~~

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | <code>/api/auth/register</code> | Public | Register a member |
| POST | <code>/api/auth/login</code> | Public | Validate credentials and issue JWT |
| GET | <code>/api/auth/me</code> | Authenticated | Current user |
| GET | <code>/api/books?search=java&page=0&size=10</code> | Authenticated | Search and paginate books |
| GET | <code>/api/books/{id}</code> | Authenticated | Get a book |
| POST | <code>/api/books</code> | ADMIN | Add a book |
| PUT | <code>/api/books/{id}</code> | ADMIN | Update a book |
| DELETE | <code>/api/books/{id}</code> | ADMIN | Delete a book with no loan history |
| POST | <code>/api/loans/borrow/{bookId}</code> | MEMBER / ADMIN | Borrow a book |
| POST | <code>/api/loans/{loanId}/return</code> | Owner / ADMIN | Return a borrowed book |
| GET | <code>/api/loans/my</code> | Authenticated | Current user's loans |
| GET | <code>/api/loans/all</code> | ADMIN | All loans |
| GET | <code>/api/loans/overdue</code> | ADMIN | Overdue active loans |

## Example API flow

Register:

~~~bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Ali Valiyev","username":"ali","password":"strong-password-123"}'
~~~

Login and copy the returned <code>accessToken</code>:

~~~bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"ali","password":"strong-password-123"}'
~~~

Use the token to list books or borrow an available book:

~~~bash
curl http://localhost:8080/api/books \
  -H "Authorization: Bearer <access-token>"

curl -X POST http://localhost:8080/api/loans/borrow/1 \
  -H "Authorization: Bearer <access-token>"
~~~

In Swagger UI, use **Authorize** and paste the access token. Do not include the word <code>Bearer</code> if the Swagger UI scheme adds it automatically.

## Tests

The unit tests use JUnit 5 and Mockito. The integration test starts the Spring application against PostgreSQL and verifies Flyway schema creation, registration, login/JWT decoding, borrowing, and returning.

Run the full test suite with PostgreSQL available. When running tests locally, export the connection settings and a private test signing key first:

~~~bash
export DB_URL=jdbc:postgresql://localhost:5433/library
export DB_USER=postgres
export DB_PASSWORD=your-local-database-password
export JWT_SECRET_BASE64="$(openssl rand -base64 32)"
./mvnw -B clean verify
~~~

On Windows PowerShell, set the equivalent `$env:DB_URL`, `$env:DB_USER`, `$env:DB_PASSWORD`, and `$env:JWT_SECRET_BASE64` variables before running `mvnw.cmd -B clean verify`.

The PostgreSQL integration test runs in a transaction and rolls its changes back. It uses unique test records and does not delete existing books, users or loans in your local database.

GitHub Actions starts an isolated PostgreSQL 17 service and runs the same Maven command on every push and pull request to <code>main</code>.

## Project structure

~~~text
com.library.demo
├── config/       Security, JWT, OpenAPI and seed data
├── controller/   Auth, Book and Loan REST endpoints
├── dto/          Validated request and response records
├── entity/       AppUser, Book, Loan and Role
├── exception/    API errors and exception handling
├── repository/   Spring Data JPA and locking queries
└── service/      Authentication, inventory and loan rules
~~~

## Design notes and current scope

- JWT access tokens expire after 15 minutes. Refresh-token rotation and token revocation are not implemented.
- Payments, email notifications and a frontend are outside this API's scope.
- The bootstrap administrator is optional and must be configured with environment variables; public registration cannot create an administrator.
- The included <code>.env.example</code> is for local development only. Use secret storage and unique credentials in any hosted environment.
