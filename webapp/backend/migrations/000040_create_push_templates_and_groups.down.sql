ALTER TABLE push_campaigns DROP COLUMN IF EXISTS template_id, DROP COLUMN IF EXISTS target_group_id;
DROP TABLE IF EXISTS user_group_members;
DROP TABLE IF EXISTS user_groups;
DROP TABLE IF EXISTS push_templates;
