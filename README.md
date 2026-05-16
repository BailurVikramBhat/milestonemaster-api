# Milestone Master API

Backend API for Milestone Master, built with Spring Boot, Java, PostgreSQL, Maven, and Spring Security.

## Prerequisites

- Java 25
- Maven 3.9+
- Docker Desktop
- Git

## Environment Files

Create a local `.env` file from `.env.example`:

```powershell
Copy-Item .env.example .env
```

Update `.env` with local values:

```env
POSTGRES_DB=milestonemaster
POSTGRES_USER=milestonemaster
POSTGRES_PASSWORD=milestonemaster
POSTGRES_PORT=5432
JWT_SECRET=replace-with-a-long-base64-secret
JWT_EXPIRATION_MINUTES=15
```

Do not commit `.env`. Commit only `.env.example`.

## Local Development

Start the local PostgreSQL container:

```powershell
docker compose --profile local up -d postgres
```

Run the application with the `dev` profile:

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The `dev` profile uses PostgreSQL on `localhost:5432` and `ddl-auto=update` for early development.

Stop local infrastructure:

```powershell
docker compose --profile local down
```

Reset the local database volume:

```powershell
docker compose --profile local down -v
```

## Automated Tests

Run tests:

```powershell
mvn test
```

Tests use the `test` profile and an in-memory H2 database configured in `src/test/resources/application-test.yaml`.

## Logging

The API adds an `X-Request-Id` header to every HTTP response. If a client sends `X-Request-Id`, the API reuses it; otherwise it generates one. The same value appears in application logs so a single request can be traced across filters, controllers, services, and exception handling.

Each HTTP request also logs one summary line:

```text
POST /api/v1/auth/login -> 200 in 92ms
```

Log levels are configured per profile:

| Profile | Behavior |
| --- | --- |
| default | App logs at `INFO`, Spring Security and SQL logs quieter |
| `dev` | App logs at `DEBUG`, SQL statements enabled through logger config |
| `test` | App logs mostly quiet to keep test output readable |
| `prod` | App logs at `INFO`, framework and SQL logs kept quiet |

You can override the console pattern with `LOGPATTERN_CONSOLE`. Sensitive values such as passwords, JWTs, cookies, and request bodies should not be logged. Auth-related logs mask email addresses where useful.

## Coverage

Run tests with coverage checks:

```powershell
mvn clean verify
```

Open the JaCoCo report:

```powershell
Start-Process .\target\site\jacoco\index.html
```

The build fails if coverage drops below the configured JaCoCo threshold in `pom.xml`.

## Environment Profiles

Current profiles:

| Profile | Purpose | Database |
| --- | --- | --- |
| `dev` | Local development | Docker PostgreSQL |
| `test` | Automated tests | H2 in-memory |

Planned deployment profiles:

| Environment | Profile | Backend Port | Database Port |
| --- | --- | ---: | ---: |
| Test | `testenv` | `8081` | `5433` |
| Pre-prod | `preprod` | `8082` | `5434` |
| Production | `prod` | `8083` | `5435` |

Production configuration should be environment-variable driven. Do not store production secrets in YAML files.

## CI

GitHub Actions runs Maven verification for builds, tests, and coverage:

```powershell
mvn clean verify
```

Required status checks can be configured after the workflow has run at least once on GitHub.
