# Project Context

## Authentication Identity
- JWT subject/username is used as the principal identifier.
- Identifier may be either `email` or `universityId`.
- Chat authorization checks accept both.

## Chat Rules
- A user can read/send messages only if they are a member of that chat.
- Membership checks are enforced for:
  - REST endpoints
  - WebSocket `SEND` and `SUBSCRIBE`
- Chat message payload is validated:
  - `message` is required (`@NotBlank`)
  - max length is 300

## Chat Presence and Counts
- `memberCount` in chat summary = total `chat_members` rows for chat.
- `onlineCount` in chat summary = users currently subscribed to that chat over WebSocket.
- Online presence is tracked per session:
  - `CONNECT` registers session
  - `SUBSCRIBE` marks user online in a chat
  - `UNSUBSCRIBE`/`DISCONNECT` removes presence

## Data Initialization
- Teacher-group chats are auto-created in `DataInitializer`.
- For each teacher-group pair:
  - one `TEACHER_GROUP` chat is created (idempotent)
  - teacher is added as `TEACHER`
  - group students are added as `STUDENT`

## Migrations
- Flyway is enabled (`classpath:db/migration`).
- Existing DBs are supported with `baseline-on-migrate=true`.
- Current migration:
  - `V1__create_fcm_table.sql`
    - creates `fcm`
    - FK to `users(id)`
    - unique index `(user_id, fcm_token)`
    - index on `user_id`

## Notes
- `ddl-auto` is still `update` in current profiles for backward compatibility.
- Long-term: migrate fully to Flyway-owned schema and switch `ddl-auto` to `validate`.
