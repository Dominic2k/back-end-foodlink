USE foodlink_db;

-- =========================
-- USERS
-- =========================
INSERT INTO users (user_id, email, password_hash, full_name, status, is_admin)
VALUES
    (
        'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        'admin@foodlink.com',
        '$2a$10$7qNQyYxH6F4dZP4vYwX4Oe6Vt2nT6zvZPpOZP5lZzM5pJ1ZJ9nJ6a',
        'FoodLink Admin',
        'active',
        1
    ),
    (
        'b2c3d4e5-f6a7-8901-bcde-f12345678901',
        'user@foodlink.com',
        '$2a$10$7qNQyYxH6F4dZP4vYwX4Oe6Vt2nT6zvZPpOZP5lZzM5pJ1ZJ9nJ6a',
        'Test User',
        'active',
        0
    );

-- =========================
-- FAMILY MEMBERS
-- =========================
-- Self
INSERT INTO family_members (member_id, user_id, display_name, relationship, gender, activity_level)
VALUES (
    'c3d4e5f6-a7b8-9012-cdef-123456789012',
    'b2c3d4e5-f6a7-8901-bcde-f12345678901',
    'Test User',
    'self',
    'male',
    'medium'
);

-- Mother
INSERT INTO family_members (member_id, user_id, display_name, relationship, gender, activity_level)
VALUES (
    'd4e5f6a7-b8c9-0123-def1-234567890123',
    'b2c3d4e5-f6a7-8901-bcde-f12345678901',
    'Mother',
    'mother',
    'female',
    'low'
);

-- Child
INSERT INTO family_members (member_id, user_id, display_name, relationship, gender, activity_level)
VALUES (
    'e5f6a7b8-c9d0-1234-ef12-345678901234',
    'b2c3d4e5-f6a7-8901-bcde-f12345678901',
    'Child',
    'child',
    'male',
    'high'
);

-- =========================
-- HEALTH CONDITIONS
-- =========================
INSERT INTO health_conditions (condition_id, code, name)
VALUES
    ('f6a7b8c9-d0e1-2345-f123-456789012345', 'DIAB', 'Diabetes'),
    ('a7b8c9d0-e1f2-3456-0123-567890123456', 'HYP', 'Hypertension'),
    ('b8c9d0e1-f2a3-4567-1234-678901234567', 'ALL', 'Food Allergy'),
    ('c9d0e1f2-a3b4-5678-2345-789012345678', 'OBE', 'Obesity'),
    ('d0e1f2a3-b4c5-6789-3456-890123456789', 'CEL', 'Celiac Disease');

-- =========================
-- MEMBER CONDITIONS
-- =========================
-- Test User has Hypertension
INSERT INTO member_conditions (member_id, condition_id)
VALUES (
    'c3d4e5f6-a7b8-9012-cdef-123456789012',
    'a7b8c9d0-e1f2-3456-0123-567890123456'
);

-- Child has Obesity
INSERT INTO member_conditions (member_id, condition_id)
VALUES (
    'e5f6a7b8-c9d0-1234-ef12-345678901234',
    'c9d0e1f2-a3b4-5678-2345-789012345678'
);

-- =========================
-- INGREDIENTS
-- =========================
INSERT INTO ingredients (ingredient_id, name, category, default_unit)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'Chicken Breast', 'meat', 'g'),
    ('22222222-2222-2222-2222-222222222222', 'Egg', 'protein', 'piece'),
    ('33333333-3333-3333-3333-333333333333', 'Milk', 'dairy', 'ml'),
    ('44444444-4444-4444-4444-444444444444', 'Rice', 'grain', 'g'),
    ('55555555-5555-5555-5555-555555555555', 'Broccoli', 'vegetable', 'g'),
    ('66666666-6666-6666-6666-666666666666', 'Peanut', 'nut', 'g');

-- =========================
-- INGREDIENT NUTRITION
-- =========================
INSERT INTO ingredient_nutrition (ingredient_id, calories_per_100, protein_g_per_100, carb_g_per_100, fat_g_per_100)
VALUES
    ('11111111-1111-1111-1111-111111111111', 165, 31, 0, 3.6),
    ('22222222-2222-2222-2222-222222222222', 155, 13, 1.1, 11),
    ('33333333-3333-3333-3333-333333333333', 42, 3.4, 5, 1),
    ('44444444-4444-4444-4444-444444444444', 130, 2.7, 28, 0.3),
    ('55555555-5555-5555-5555-555555555555', 34, 2.8, 7, 0.4),
    ('66666666-6666-6666-6666-666666666666', 567, 26, 16, 49);

-- =========================
-- MEMBER ALLERGIES
-- =========================
-- Mother is allergic to Peanut
INSERT INTO member_allergies (allergy_id, member_id, ingredient_id, severity)
VALUES (
    '77777777-7777-7777-7777-777777777777',
    'd4e5f6a7-b8c9-0123-def1-234567890123',
    '66666666-6666-6666-6666-666666666666',
    'severe'
);
