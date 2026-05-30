# RSPCM Backend

Spring Boot backend for managing users, groups, subjects, exams, practices, submissions, and practice journals.

## Tech Stack

- Java 17
- Spring Boot 3.4.x
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Gradle
- Swagger/OpenAPI (`springdoc`)

## Prerequisites

- JDK 17+
- PostgreSQL running locally or remotely
- Gradle wrapper (`./gradlew`) is included

## Configuration

Main config is in:

- `src/main/resources/application.yaml`
- `src/main/resources/application-local.yaml`
- `src/main/resources/application-prod.yaml`

Important env vars:

- `JWT_SECRET`
- `JWT_EXPIRATION_MINUTES`
- `OTP_EXPIRATION_MINUTES`
- `MAIL_FROM`
- `INMEMORY_ADMIN_ENABLED`
- `INMEMORY_ADMIN_USERNAME`
- `INMEMORY_ADMIN_PASSWORD`
- `INMEMORY_ADMIN_FULL_NAME`
- `APP_UPLOAD_DIR`

Also configure datasource and mail settings in your active profile file (`application-local.yaml` for local development).

## File Upload Storage

- Local development:
  - `app.upload-dir=uploads`
  - Files are stored under:
    - `uploads/chats/`
    - `uploads/groups/`
    - `uploads/users/`

- Production:
  - `app.upload-dir=/opt/backend/uploads` (or `/var/app/uploads`)
  - Keep uploads outside the JAR directory so redeploying `app.jar` does not remove uploaded files.
  - App auto-creates:
    - `uploads/chats/`
    - `uploads/groups/`
    - `uploads/users/`
    - `uploads/images/`

## Run Locally

```bash
./gradlew bootRun
```

Default profile from `application.yaml` is:

- `local`

## Build

```bash
./gradlew clean build
```

## API Docs

After starting the app:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Authentication

Public auth endpoints:

- `POST /api/auth/login`
- `POST /api/auth/verify-otp`
- `POST /api/auth/resend-otp`

Most other endpoints require JWT Bearer token.

## Key Endpoint Groups

- `/api/admin-dashboard`
- `/api/users`
- `/api/profile`
- `/api/groups` (admin/teacher/student variants)
- `/api/subjects`
- `/api/exams`
- `/api/exam-practices`
- `/api/practice-participations`
- `/api/practice-submissions`
- `/api/practice-journals`

## Notes

- CORS and security are configured in `config/`.
- Global error handling is in `exception/GlobalExceptionHandler`.
- Initial system bootstrap logic is in `config/DataInitializer`.
