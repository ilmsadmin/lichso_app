DROP INDEX IF EXISTS idx_quiz_sessions_client_session_id;
ALTER TABLE quiz_sessions DROP COLUMN IF EXISTS client_session_id;
