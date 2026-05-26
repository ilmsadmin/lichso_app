ALTER TABLE quiz_sessions
ADD COLUMN IF NOT EXISTS client_session_id VARCHAR(100);

CREATE UNIQUE INDEX IF NOT EXISTS idx_quiz_sessions_client_session_id
ON quiz_sessions(client_session_id)
WHERE client_session_id IS NOT NULL;
