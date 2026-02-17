-- WilliamMacMiron Seed Data V2
-- Initial data for languages, roles, basic categories, and flavors

-- =====================================================
-- 1. LANGUAGES
-- =====================================================
INSERT INTO languages (code, name, native_name, is_default, is_active, display_order)
VALUES ('pl', 'Polish', 'Polski', TRUE, TRUE, 1),
       ('en', 'English', 'English', FALSE, TRUE, 2);

-- =====================================================
-- 2. ROLES
-- =====================================================
INSERT INTO roles (name, description)
VALUES ('ROLE_ADMIN', 'Full system access including user management and system settings'),
       ('ROLE_EDITOR', 'Can manage content (create, edit, publish)'),
       ('ROLE_VIEWER', 'Read-only access to admin panel');

-- =====================================================
-- 3. DEFAULT ADMIN USER
-- =====================================================
INSERT INTO users (email, password_hash, password_algo, display_name, is_active, is_email_verified)
VALUES ('admin@williammacmiron.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'bcrypt', 'Admin',
        TRUE, TRUE);

-- Assign ADMIN role to default admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u,
     roles r
WHERE u.email = 'admin@williammacmiron.com'
  AND r.name = 'ROLE_ADMIN';

-- =====================================================
-- 4. BASIC SETTINGS
-- =====================================================
INSERT INTO settings (key, value, value_type, is_public, description)
VALUES ('site.name', 'WilliamMacMiron', 'string', TRUE, 'Site name'),
       ('site.tagline', 'Your guide to fine spirits', 'string', TRUE, 'Site tagline'),
       ('site.default_language', 'pl', 'string', TRUE, 'Default language code'),
       ('site.enable_age_verification', 'true', 'boolean', TRUE, 'Enable age verification popup'),
       ('site.minimum_age', '18', 'integer', TRUE, 'Minimum age for site access'),
       ('site.items_per_page', '12', 'integer', FALSE, 'Items per page in listings'),
       ('site.enable_search', 'true', 'boolean', TRUE, 'Enable search functionality'),
       ('site.enable_tasting_notes', 'true', 'boolean', TRUE, 'Enable tasting notes feature'),
       ('admin.require_translation_for_publish', 'true', 'boolean', FALSE,
        'Require default language translation before publishing'),
       ('media.max_file_size_mb', '10', 'integer', FALSE, 'Maximum file size for uploads in MB'),
       ('media.allowed_extensions', '["jpg","jpeg","png","webp"]', 'json', FALSE, 'Allowed image file extensions');

-- =====================================================
-- 5. SAMPLE FLAVORS (Whisky-focused)
-- =====================================================

-- Get language IDs
DO
$$
    DECLARE
        lang_pl_id BIGINT;
        lang_en_id BIGINT;
    BEGIN
        SELECT id INTO lang_pl_id FROM languages WHERE code = 'pl';
        SELECT id INTO lang_en_id FROM languages WHERE code = 'en';

        -- Insert flavors with translations

        -- Fruity flavors
        INSERT INTO flavors (slug, color, display_order, is_active) VALUES ('apple', '#90EE90', 1, TRUE);
        INSERT INTO flavor_translations (flavor_id, language_id, name, description)
        VALUES (currval('flavors_id_seq'), lang_pl_id, 'Jabłko', 'Świeże, soczyste nuty jabłkowe'),
               (currval('flavors_id_seq'), lang_en_id, 'Apple', 'Fresh, juicy apple notes');

        INSERT INTO flavors (slug, color, display_order, is_active) VALUES ('pear', '#DDA15E', 2, TRUE);
        INSERT INTO flavor_translations (flavor_id, language_id, name, description)
        VALUES (currval('flavors_id_seq'), lang_pl_id, 'Gruszka', 'Słodkie nuty gruszki'),
               (currval('flavors_id_seq'), lang_en_id, 'Pear', 'Sweet pear notes');

        INSERT INTO flavors (slug, color, display_order, is_active) VALUES ('citrus', '#FFD700', 3, TRUE);
        INSERT INTO flavor_translations (flavor_id, language_id, name, description)
        VALUES (currval('flavors_id_seq'), lang_pl_id, 'Cytrusy', 'Nuty cytrusowe, pomarańcza, cytryna'),
               (currval('flavors_id_seq'), lang_en_id, 'Citrus', 'Citrus notes, orange, lemon');

        INSERT INTO flavors (slug, color, display_order, is_active) VALUES ('dried-fruit', '#8B4513', 4, TRUE);
        INSERT INTO flavor_translations (flavor_id, language_id, name, description)
        VALUES (currval('flavors_id_seq'), lang_pl_id, 'Suszone owoce', 'Rodzynki, daktyle, figi'),
               (currval('flavors_id_seq'), lang_en_id, 'Dried fruit', 'Raisins, dates, figs');

        -- Sweet flavors
        INSERT INTO flavors (slug, color, display_order, is_active) VALUES ('honey', '#FFA500', 10, TRUE);
        INSERT INTO flavor_translations (flavor_id, language_id, name, description)
        VALUES (currval('flavors_id_seq'), lang_pl_id, 'Miód', 'Słodkie nuty miodowe'),
               (currval('flavors_id_seq'), lang_en_id, 'Honey', 'Sweet honey notes');

        INSERT INTO flavors (slug, color, display_order, is_active) VALUES ('vanilla', '#F5DEB3', 11, TRUE);
        INSERT INTO flavor_translations (flavor_id, language_id, name, description)
        VALUES (currval('flavors_id_seq'), lang_pl_id, 'Wanilia', 'Kremowa wanilia'),
               (currval('flavors_id_seq'), lang_en_id, 'Vanilla', 'Creamy vanilla');

        INSERT INTO flavors (slug, color, display_order, is_active) VALUES ('caramel', '#D2691E', 12, TRUE);
        INSERT INTO flavor_translations (flavor_id, language_id, name, description)
        VALUES (currval('flavors_id_seq'), lang_pl_id, 'Karmel', 'Słodki karmel, toffi'),
               (currval('flavors_id_seq'), lang_en_id, 'Caramel', 'Sweet caramel, toffee');

        INSERT INTO flavors (slug, color, display_order, is_active) VALUES ('chocolate', '#4B3621', 13, TRUE);
        INSERT INTO flavor_translations (flavor_id, language_id, name, description)
        VALUES (currval('flavors_id_seq'), lang_pl_id, 'Czekolada', 'Nuty czekoladowe, kakao'),
               (currval('flavors_id_seq'), lang_en_id, 'Chocolate', 'Chocolate notes, cocoa');

        -- Spicy flavors
        INSERT INTO flavors (slug, color, display_order, is_active) VALUES ('cinnamon', '#A0522D', 20, TRUE);
        INSERT INTO flavor_translations (flavor_id, language_id, name, description)
        VALUES (currval('flavors_id_seq'), lang_pl_id, 'Cynamon', 'Ciepły cynamon'),
               (currval('flavors_id_seq'), lang_en_id, 'Cinnamon', 'Warm cinnamon');

        INSERT INTO flavors (slug, color, display_order, is_active) VALUES ('pepper', '#696969', 21, TRUE);
        INSERT INTO flavor_translations (flavor_id, language_id, name, description)
        VALUES (currval('flavors_id_seq'), lang_pl_id, 'Pieprz', 'Ostre nuty pieprzowe'),
               (currval('flavors_id_seq'), lang_en_id, 'Pepper', 'Peppery spice notes');

        INSERT INTO flavors (slug, color, display_order, is_active) VALUES ('ginger', '#CD853F', 22, TRUE);
        INSERT INTO flavor_translations (flavor_id, language_id, name, description)
        VALUES (currval('flavors_id_seq'), lang_pl_id, 'Imbir', 'Pikantny imbir'),
               (currval('flavors_id_seq'), lang_en_id, 'Ginger', 'Spicy ginger');

        -- Woody flavors
        INSERT INTO flavors (slug, color, display_order, is_active) VALUES ('oak', '#8B7355', 30, TRUE);
        INSERT INTO flavor_translations (flavor_id, language_id, name, description)
        VALUES (currval('flavors_id_seq'), lang_pl_id, 'Dąb', 'Nuty dębowe z beczek'),
               (currval('flavors_id_seq'), lang_en_id, 'Oak', 'Oak barrel notes');

        INSERT INTO flavors (slug, color, display_order, is_active) VALUES ('smoke', '#2F4F4F', 31, TRUE);
        INSERT INTO flavor_translations (flavor_id, language_id, name, description)
        VALUES (currval('flavors_id_seq'), lang_pl_id, 'Dym', 'Dymne, torfowe nuty'),
               (currval('flavors_id_seq'), lang_en_id, 'Smoke', 'Smoky, peaty notes');

        INSERT INTO flavors (slug, color, display_order, is_active) VALUES ('cedar', '#A0826D', 32, TRUE);
        INSERT INTO flavor_translations (flavor_id, language_id, name, description)
        VALUES (currval('flavors_id_seq'), lang_pl_id, 'Cedr', 'Drewno cedrowe'),
               (currval('flavors_id_seq'), lang_en_id, 'Cedar', 'Cedar wood');

        -- Floral/Herbal
        INSERT INTO flavors (slug, color, display_order, is_active) VALUES ('floral', '#FFB6C1', 40, TRUE);
        INSERT INTO flavor_translations (flavor_id, language_id, name, description)
        VALUES (currval('flavors_id_seq'), lang_pl_id, 'Kwiatowe', 'Delikatne nuty kwiatowe'),
               (currval('flavors_id_seq'), lang_en_id, 'Floral', 'Delicate floral notes');

        INSERT INTO flavors (slug, color, display_order, is_active) VALUES ('herbal', '#6B8E23', 41, TRUE);
        INSERT INTO flavor_translations (flavor_id, language_id, name, description)
        VALUES (currval('flavors_id_seq'), lang_pl_id, 'Ziołowe', 'Nuty ziołowe, trawę'),
               (currval('flavors_id_seq'), lang_en_id, 'Herbal', 'Herbal, grassy notes');

        -- Marine/Coastal
        INSERT INTO flavors (slug, color, display_order, is_active) VALUES ('maritime', '#20B2AA', 50, TRUE);
        INSERT INTO flavor_translations (flavor_id, language_id, name, description)
        VALUES (currval('flavors_id_seq'), lang_pl_id, 'Morskie', 'Nuty morskie, solone'),
               (currval('flavors_id_seq'), lang_en_id, 'Maritime', 'Maritime, salty notes');

        INSERT INTO flavors (slug, color, display_order, is_active) VALUES ('iodine', '#4682B4', 51, TRUE);
        INSERT INTO flavor_translations (flavor_id, language_id, name, description)
        VALUES (currval('flavors_id_seq'), lang_pl_id, 'Jodyna', 'Intensywne nuty jodyny'),
               (currval('flavors_id_seq'), lang_en_id, 'Iodine', 'Intense iodine notes');

        -- Nutty
        INSERT INTO flavors (slug, color, display_order, is_active) VALUES ('almond', '#FFEBCD', 60, TRUE);
        INSERT INTO flavor_translations (flavor_id, language_id, name, description)
        VALUES (currval('flavors_id_seq'), lang_pl_id, 'Migdały', 'Nuty migdałowe'),
               (currval('flavors_id_seq'), lang_en_id, 'Almond', 'Almond notes');

        INSERT INTO flavors (slug, color, display_order, is_active) VALUES ('walnut', '#8B6914', 61, TRUE);
        INSERT INTO flavor_translations (flavor_id, language_id, name, description)
        VALUES (currval('flavors_id_seq'), lang_pl_id, 'Orzechy włoskie', 'Nuty orzechów włoskich'),
               (currval('flavors_id_seq'), lang_en_id, 'Walnut', 'Walnut notes');

    END
$$;

-- =====================================================
-- 6. SAMPLE CATEGORY: WHISKY
-- =====================================================

DO
$$
    DECLARE
        lang_pl_id         BIGINT;
        lang_en_id         BIGINT;
        category_whisky_id BIGINT;
    BEGIN
        SELECT id INTO lang_pl_id FROM languages WHERE code = 'pl';
        SELECT id INTO lang_en_id FROM languages WHERE code = 'en';

        -- Create Whisky category
        INSERT INTO categories (slug, icon, display_order, is_active, status, published_at)
        VALUES ('whisky', 'whiskey-glass', 1, TRUE, 'published', CURRENT_TIMESTAMP)
        RETURNING id INTO category_whisky_id;

        INSERT INTO category_translations (category_id, language_id, title, description, meta_title, meta_description)
        VALUES (category_whisky_id, lang_pl_id, 'Whisky',
                'Odkryj świat whisky - od szkockiej single malt po amerykański bourbon.',
                'Whisky - Przewodnik po najlepszych markach',
                'Kompletny przewodnik po świecie whisky. Recenzje, oceny i notatki degustacyjne.'),
               (category_whisky_id, lang_en_id, 'Whisky',
                'Discover the world of whisky - from Scottish single malt to American bourbon.',
                'Whisky - Guide to the Best Brands',
                'Complete guide to the world of whisky. Reviews, ratings and tasting notes.');

    END
$$;

