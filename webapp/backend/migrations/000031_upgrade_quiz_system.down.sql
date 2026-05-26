-- 1. Xoá các cột bổ sung
ALTER TABLE quiz_sessions DROP COLUMN IF EXISTS score_v2;
ALTER TABLE quiz_scores DROP COLUMN IF EXISTS xp;

ALTER TABLE quiz_questions DROP COLUMN IF EXISTS version;
ALTER TABLE quiz_questions DROP COLUMN IF EXISTS published_at;
ALTER TABLE quiz_questions DROP COLUMN IF EXISTS reviewed_at;
ALTER TABLE quiz_questions DROP COLUMN IF EXISTS reviewed_by;
ALTER TABLE quiz_questions DROP COLUMN IF EXISTS review_status;
ALTER TABLE quiz_questions DROP COLUMN IF EXISTS source_note;
ALTER TABLE quiz_questions DROP COLUMN IF EXISTS source_url;
ALTER TABLE quiz_questions DROP COLUMN IF EXISTS source_title;

-- 2. Xoá các bảng đã tạo
DROP TABLE IF EXISTS user_badges CASCADE;
DROP TABLE IF EXISTS quiz_category_masteries CASCADE;
DROP TABLE IF EXISTS quiz_assist_usages CASCADE;
DROP TABLE IF EXISTS point_transactions CASCADE;
DROP TABLE IF EXISTS point_wallets CASCADE;
