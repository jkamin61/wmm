-- WilliamMacMiron Database Schema V1
-- Core tables for content management system with multi-language support

-- =====================================================
-- 1. LANGUAGES
-- =====================================================
CREATE TABLE languages (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    native_name VARCHAR(100),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_languages_is_default ON languages(is_default) WHERE is_default = TRUE;
CREATE INDEX idx_languages_is_active ON languages(is_active);

-- =====================================================
-- 2. USERS & ROLES
-- =====================================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    password_algo VARCHAR(30) NOT NULL DEFAULT 'bcrypt',
    display_name VARCHAR(120),
    avatar_path VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP WITH TIME ZONE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_is_active ON users(is_active);

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- =====================================================
-- 3. AUTH TOKENS
-- =====================================================
CREATE TABLE auth_refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,
    device_info VARCHAR(500),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_tokens_user_id ON auth_refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token_hash ON auth_refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_expires_at ON auth_refresh_tokens(expires_at);

-- =====================================================
-- 4. CATEGORIES
-- =====================================================
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(150) NOT NULL UNIQUE,
    icon VARCHAR(100),
    display_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    published_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_category_status CHECK (status IN ('draft', 'published', 'archived'))
);

CREATE INDEX idx_categories_slug ON categories(slug);
CREATE INDEX idx_categories_status ON categories(status);
CREATE INDEX idx_categories_is_active ON categories(is_active);
CREATE INDEX idx_categories_display_order ON categories(display_order);

CREATE TABLE category_translations (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    language_id BIGINT NOT NULL REFERENCES languages(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    meta_title VARCHAR(255),
    meta_description VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(category_id, language_id)
);

CREATE INDEX idx_category_translations_category_id ON category_translations(category_id);
CREATE INDEX idx_category_translations_language_id ON category_translations(language_id);

-- =====================================================
-- 5. TOPICS
-- =====================================================
CREATE TABLE topics (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    slug VARCHAR(150) NOT NULL UNIQUE,
    icon VARCHAR(100),
    display_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    published_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_topic_status CHECK (status IN ('draft', 'published', 'archived'))
);

CREATE INDEX idx_topics_category_id ON topics(category_id);
CREATE INDEX idx_topics_slug ON topics(slug);
CREATE INDEX idx_topics_status ON topics(status);
CREATE INDEX idx_topics_is_active ON topics(is_active);
CREATE INDEX idx_topics_display_order ON topics(display_order);

CREATE TABLE topic_translations (
    id BIGSERIAL PRIMARY KEY,
    topic_id BIGINT NOT NULL REFERENCES topics(id) ON DELETE CASCADE,
    language_id BIGINT NOT NULL REFERENCES languages(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    subtitle VARCHAR(255),
    description TEXT,
    meta_title VARCHAR(255),
    meta_description VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(topic_id, language_id)
);

CREATE INDEX idx_topic_translations_topic_id ON topic_translations(topic_id);
CREATE INDEX idx_topic_translations_language_id ON topic_translations(language_id);

-- =====================================================
-- 6. SUBTOPICS
-- =====================================================
CREATE TABLE subtopics (
    id BIGSERIAL PRIMARY KEY,
    topic_id BIGINT NOT NULL REFERENCES topics(id) ON DELETE CASCADE,
    slug VARCHAR(150) NOT NULL UNIQUE,
    icon VARCHAR(100),
    display_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    published_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_subtopic_status CHECK (status IN ('draft', 'published', 'archived'))
);

CREATE INDEX idx_subtopics_topic_id ON subtopics(topic_id);
CREATE INDEX idx_subtopics_slug ON subtopics(slug);
CREATE INDEX idx_subtopics_status ON subtopics(status);
CREATE INDEX idx_subtopics_is_active ON subtopics(is_active);
CREATE INDEX idx_subtopics_display_order ON subtopics(display_order);

CREATE TABLE subtopic_translations (
    id BIGSERIAL PRIMARY KEY,
    subtopic_id BIGINT NOT NULL REFERENCES subtopics(id) ON DELETE CASCADE,
    language_id BIGINT NOT NULL REFERENCES languages(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    subtitle VARCHAR(255),
    description TEXT,
    meta_title VARCHAR(255),
    meta_description VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(subtopic_id, language_id)
);

CREATE INDEX idx_subtopic_translations_subtopic_id ON subtopic_translations(subtopic_id);
CREATE INDEX idx_subtopic_translations_language_id ON subtopic_translations(language_id);

-- =====================================================
-- 7. PARTNERS
-- =====================================================
CREATE TABLE partners (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(150) NOT NULL UNIQUE,
    logo_path VARCHAR(500),
    website_url VARCHAR(500),
    display_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_partners_slug ON partners(slug);
CREATE INDEX idx_partners_is_active ON partners(is_active);

CREATE TABLE partner_translations (
    id BIGSERIAL PRIMARY KEY,
    partner_id BIGINT NOT NULL REFERENCES partners(id) ON DELETE CASCADE,
    language_id BIGINT NOT NULL REFERENCES languages(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(partner_id, language_id)
);

CREATE INDEX idx_partner_translations_partner_id ON partner_translations(partner_id);
CREATE INDEX idx_partner_translations_language_id ON partner_translations(language_id);

-- =====================================================
-- 8. FLAVORS
-- =====================================================
CREATE TABLE flavors (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(100) NOT NULL UNIQUE,
    icon VARCHAR(100),
    color VARCHAR(7),
    display_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_flavors_slug ON flavors(slug);
CREATE INDEX idx_flavors_is_active ON flavors(is_active);

CREATE TABLE flavor_translations (
    id BIGSERIAL PRIMARY KEY,
    flavor_id BIGINT NOT NULL REFERENCES flavors(id) ON DELETE CASCADE,
    language_id BIGINT NOT NULL REFERENCES languages(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(flavor_id, language_id)
);

CREATE INDEX idx_flavor_translations_flavor_id ON flavor_translations(flavor_id);
CREATE INDEX idx_flavor_translations_language_id ON flavor_translations(language_id);

-- =====================================================
-- 9. ITEMS (Main Content)
-- =====================================================
CREATE TABLE items (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    topic_id BIGINT NOT NULL REFERENCES topics(id) ON DELETE RESTRICT,
    subtopic_id BIGINT REFERENCES subtopics(id) ON DELETE SET NULL,
    partner_id BIGINT REFERENCES partners(id) ON DELETE SET NULL,
    slug VARCHAR(200) NOT NULL UNIQUE,

    -- Product attributes (language-independent)
    abv DECIMAL(5,2),
    vintage INTEGER,
    volume_ml INTEGER,
    price_pln DECIMAL(10,2),
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,

    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    published_at TIMESTAMP WITH TIME ZONE,

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_item_status CHECK (status IN ('draft', 'published', 'archived'))
);

CREATE INDEX idx_items_category_id ON items(category_id);
CREATE INDEX idx_items_topic_id ON items(topic_id);
CREATE INDEX idx_items_subtopic_id ON items(subtopic_id);
CREATE INDEX idx_items_partner_id ON items(partner_id);
CREATE INDEX idx_items_slug ON items(slug);
CREATE INDEX idx_items_status ON items(status);
CREATE INDEX idx_items_is_featured ON items(is_featured);
CREATE INDEX idx_items_published_at ON items(published_at DESC);

CREATE TABLE item_translations (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    language_id BIGINT NOT NULL REFERENCES languages(id) ON DELETE CASCADE,

    title VARCHAR(255) NOT NULL,
    subtitle VARCHAR(255),
    excerpt TEXT,
    description TEXT,

    -- SEO
    meta_title VARCHAR(255),
    meta_description VARCHAR(500),
    meta_keywords VARCHAR(500),

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(item_id, language_id)
);

CREATE INDEX idx_item_translations_item_id ON item_translations(item_id);
CREATE INDEX idx_item_translations_language_id ON item_translations(language_id);

-- Full-text search index for items
CREATE INDEX idx_item_translations_search ON item_translations
    USING gin(to_tsvector('simple', coalesce(title, '') || ' ' || coalesce(description, '')));

-- =====================================================
-- 10. IMAGES
-- =====================================================
CREATE TABLE images (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE CASCADE,

    file_path VARCHAR(500) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size_bytes BIGINT,
    mime_type VARCHAR(100),
    width INTEGER,
    height INTEGER,

    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INTEGER NOT NULL DEFAULT 0,

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_images_item_id ON images(item_id);
CREATE INDEX idx_images_is_primary ON images(is_primary);
CREATE INDEX idx_images_display_order ON images(display_order);

-- Ensure only one primary image per item
CREATE UNIQUE INDEX idx_images_item_primary ON images(item_id) WHERE is_primary = TRUE;

CREATE TABLE image_translations (
    id BIGSERIAL PRIMARY KEY,
    image_id BIGINT NOT NULL REFERENCES images(id) ON DELETE CASCADE,
    language_id BIGINT NOT NULL REFERENCES languages(id) ON DELETE CASCADE,

    alt_text VARCHAR(255),
    caption TEXT,

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(image_id, language_id)
);

CREATE INDEX idx_image_translations_image_id ON image_translations(image_id);
CREATE INDEX idx_image_translations_language_id ON image_translations(language_id);

-- =====================================================
-- 11. TASTING NOTES
-- =====================================================
CREATE TABLE tasting_notes (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL UNIQUE REFERENCES items(id) ON DELETE CASCADE,

    -- Scores (0-100 or null)
    overall_score DECIMAL(4,2),
    aroma_score DECIMAL(4,2),
    taste_score DECIMAL(4,2),
    finish_score DECIMAL(4,2),

    -- Overall intensity (1-3: light, medium, bold)
    intensity SMALLINT,

    tasting_date DATE,
    tasted_by VARCHAR(150),

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_intensity CHECK (intensity BETWEEN 1 AND 3)
);

CREATE INDEX idx_tasting_notes_item_id ON tasting_notes(item_id);
CREATE INDEX idx_tasting_notes_overall_score ON tasting_notes(overall_score DESC);

CREATE TABLE tasting_note_translations (
    id BIGSERIAL PRIMARY KEY,
    tasting_note_id BIGINT NOT NULL REFERENCES tasting_notes(id) ON DELETE CASCADE,
    language_id BIGINT NOT NULL REFERENCES languages(id) ON DELETE CASCADE,

    aroma_notes TEXT,
    taste_notes TEXT,
    finish_notes TEXT,
    overall_impression TEXT,

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(tasting_note_id, language_id)
);

CREATE INDEX idx_tasting_note_translations_tasting_note_id ON tasting_note_translations(tasting_note_id);
CREATE INDEX idx_tasting_note_translations_language_id ON tasting_note_translations(language_id);

-- =====================================================
-- 12. FLAVOR PROFILES (Aroma, Taste, Finish)
-- =====================================================
CREATE TABLE aroma_flavors (
    id BIGSERIAL PRIMARY KEY,
    tasting_note_id BIGINT NOT NULL REFERENCES tasting_notes(id) ON DELETE CASCADE,
    flavor_id BIGINT NOT NULL REFERENCES flavors(id) ON DELETE CASCADE,
    intensity SMALLINT NOT NULL DEFAULT 1,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_aroma_intensity CHECK (intensity BETWEEN 1 AND 3),
    UNIQUE(tasting_note_id, flavor_id)
);

CREATE INDEX idx_aroma_flavors_tasting_note_id ON aroma_flavors(tasting_note_id);
CREATE INDEX idx_aroma_flavors_flavor_id ON aroma_flavors(flavor_id);

CREATE TABLE taste_flavors (
    id BIGSERIAL PRIMARY KEY,
    tasting_note_id BIGINT NOT NULL REFERENCES tasting_notes(id) ON DELETE CASCADE,
    flavor_id BIGINT NOT NULL REFERENCES flavors(id) ON DELETE CASCADE,
    intensity SMALLINT NOT NULL DEFAULT 1,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_taste_intensity CHECK (intensity BETWEEN 1 AND 3),
    UNIQUE(tasting_note_id, flavor_id)
);

CREATE INDEX idx_taste_flavors_tasting_note_id ON taste_flavors(tasting_note_id);
CREATE INDEX idx_taste_flavors_flavor_id ON taste_flavors(flavor_id);

CREATE TABLE finish_flavors (
    id BIGSERIAL PRIMARY KEY,
    tasting_note_id BIGINT NOT NULL REFERENCES tasting_notes(id) ON DELETE CASCADE,
    flavor_id BIGINT NOT NULL REFERENCES flavors(id) ON DELETE CASCADE,
    intensity SMALLINT NOT NULL DEFAULT 1,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_finish_intensity CHECK (intensity BETWEEN 1 AND 3),
    UNIQUE(tasting_note_id, flavor_id)
);

CREATE INDEX idx_finish_flavors_tasting_note_id ON finish_flavors(tasting_note_id);
CREATE INDEX idx_finish_flavors_flavor_id ON finish_flavors(flavor_id);

-- =====================================================
-- 13. SETTINGS
-- =====================================================
CREATE TABLE settings (
    id BIGSERIAL PRIMARY KEY,
    key VARCHAR(100) NOT NULL UNIQUE,
    value TEXT,
    value_type VARCHAR(30) NOT NULL DEFAULT 'string',
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_value_type CHECK (value_type IN ('string', 'integer', 'boolean', 'json'))
);

CREATE INDEX idx_settings_key ON settings(key);
CREATE INDEX idx_settings_is_public ON settings(is_public);

-- =====================================================
-- 14. AUDIT LOG
-- =====================================================
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_values JSONB,
    new_values JSONB,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_action CHECK (action IN ('create', 'update', 'delete', 'publish', 'unpublish', 'archive'))
);

CREATE INDEX idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX idx_audit_log_entity_type ON audit_log(entity_type);
CREATE INDEX idx_audit_log_entity_id ON audit_log(entity_id);
CREATE INDEX idx_audit_log_action ON audit_log(action);
CREATE INDEX idx_audit_log_created_at ON audit_log(created_at DESC);

