-- Migration 000026: Add 'ai_pending' to article_status enum
ALTER TYPE article_status ADD VALUE IF NOT EXISTS 'ai_pending';
