CREATE TABLE practice_submission_attempts (
    id                BIGSERIAL PRIMARY KEY,
    submission_id     BIGINT        NOT NULL REFERENCES practice_assignments(id),
    text_answer       TEXT,
    file_url          VARCHAR(500),
    submitted_at      TIMESTAMP     NOT NULL,
    attempt_number    INTEGER       NOT NULL
);
