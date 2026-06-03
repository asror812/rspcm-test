ALTER TABLE practice_submission_attempts
    ADD COLUMN IF NOT EXISTS teacher_comment VARCHAR(2000);
