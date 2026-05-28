# Backend Context (Short)

## Role In System
`rspcm` is the Spring Boot backend for the RSPCM education platform.
It is the source of truth for business logic, data, auth, and permissions.

## Core Domains
- Authentication (JWT + OTP).
- Users/roles/profiles (admin, teacher, student).
- Groups and subjects.
- Exams/questions/answers.
- Practices, participation teams, submissions, and journals.
- Dashboards and chat.

## Architecture
- `controller/`: REST + websocket endpoints.
- `service/`: business logic.
- `repository/`: persistence access.
- `model/entity/`: JPA entities.
- `dto/` + `mapper/`: API contract and mapping.

## Runtime/Config
- Java 17, Spring Boot, JPA, PostgreSQL, Gradle.
- Config files: `src/main/resources/application*.yaml`.
- Local run: `./gradlew bootRun` (default profile: `local`).
- API docs: `/swagger-ui.html`, `/v3/api-docs`.

## Integration Notes
- Any contract change should be coordinated with mobile:
  1. controller DTOs,
  2. service behavior,
  3. mobile models/services/screens.
