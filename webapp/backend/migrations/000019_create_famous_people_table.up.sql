-- Famous People: notable historical and contemporary figures
CREATE TABLE IF NOT EXISTS famous_people (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255),
    birth_date DATE,
    birth_day INT,
    birth_month INT,
    birth_year INT,
    death_date DATE,
    nationality VARCHAR(100),
    occupation VARCHAR(500),
    category VARCHAR(50) NOT NULL DEFAULT 'khac',
    short_bio TEXT,
    image_url VARCHAR(500),
    article_id UUID REFERENCES articles(id) ON DELETE SET NULL,
    is_vietnamese BOOLEAN DEFAULT FALSE,
    tags TEXT[] DEFAULT '{}',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_famous_people_birthday ON famous_people(birth_month, birth_day) WHERE is_active = TRUE;
CREATE INDEX idx_famous_people_category ON famous_people(category);
CREATE INDEX idx_famous_people_nationality ON famous_people(nationality);
CREATE INDEX idx_famous_people_vietnamese ON famous_people(is_vietnamese) WHERE is_vietnamese = TRUE;
CREATE INDEX idx_famous_people_tags ON famous_people USING GIN(tags);
CREATE INDEX idx_famous_people_article ON famous_people(article_id);
