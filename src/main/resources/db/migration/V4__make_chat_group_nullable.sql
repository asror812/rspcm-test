-- Allow direct (1-on-1) chats that are not tied to a study group
ALTER TABLE chats ALTER COLUMN group_id DROP NOT NULL;
