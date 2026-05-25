CREATE TABLE IF NOT EXISTS newsletter_subscribers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(200),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    frequency VARCHAR(20) NOT NULL DEFAULT 'daily',
    preferences JSONB NOT NULL DEFAULT '{}',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    confirmed_at TIMESTAMPTZ,
    unsubscribed_at TIMESTAMPTZ,
    last_sent_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_newsletter_subscribers_email ON newsletter_subscribers(email);
CREATE INDEX idx_newsletter_subscribers_is_active ON newsletter_subscribers(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_newsletter_subscribers_frequency ON newsletter_subscribers(frequency);
CREATE INDEX idx_newsletter_subscribers_user_id ON newsletter_subscribers(user_id);
