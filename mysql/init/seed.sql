USE foodlink_db;

-- =========================
-- USERS
-- =========================
INSERT INTO users (email, password_hash, full_name, status, is_admin)
VALUES
    ('admin@foodlink.com',
     '$2a$10$7qNQyYxH6F4dZP4vYwX4Oe6Vt2nT6zvZPpOZP5lZzM5pJ1ZJ9nJ6a',
     'FoodLink Admin',
     'active',
     1),
    ('user@foodlink.com',
     '$2a$10$7qNQyYxH6F4dZP4vYwX4Oe6Vt2nT6zvZPpOZP5lZzM5pJ1ZJ9nJ6a',
     'Test User',
     'active',
     0);

-- =========================
-- HOUSEHOLD (for test user)
-- =========================
INSERT INTO households (owner_user_id, name)
SELECT user_id, 'Test User Family'
FROM users
WHERE email = 'user@foodlink.com';

-- =========================
-- HOUSEHOLD MEMBERS
-- =========================
-- Self
INSERT INTO household_members (household_id, display_name, relationship, gender, activity_level)
SELECT h.household_id, 'Test User', 'self', 'male', 'medium'
FROM households h
         JOIN users u ON h.owner_user_id = u.user_id
WHERE u.email = 'user@foodlink.com';

-- Mother
INSERT INTO household_members (household_id, display_name, relationship, gender, activity_level)
SELECT h.household_id, 'Mother', 'mother', 'female', 'low'
FROM households h
         JOIN users u ON h.owner_user_id = u.user_id
WHERE u.email = 'user@foodlink.com';

-- Child
INSERT INTO household_members (household_id, display_name, relationship, gender, activity_level)
SELECT h.household_id, 'Child', 'child', 'male', 'high'
FROM households h
         JOIN users u ON h.owner_user_id = u.user_id
WHERE u.email = 'user@foodlink.com';

-- =========================
-- HEALTH CONDITIONS
-- =========================
INSERT INTO health_conditions (code, name)
VALUES
    ('DIAB', 'Diabetes'),
    ('HYP', 'Hypertension'),
    ('ALL', 'Food Allergy'),
    ('OBE', 'Obesity'),
    ('CEL', 'Celiac Disease');

-- =========================
-- MEMBER CONDITIONS
-- =========================
-- Test User has Hypertension
INSERT INTO member_conditions (member_id, condition_id)
SELECT m.member_id, c.condition_id
FROM household_members m
         JOIN health_conditions c ON c.code = 'HYP'
WHERE m.display_name = 'Test User';

-- Child has Obesity
INSERT INTO member_conditions (member_id, condition_id)
SELECT m.member_id, c.condition_id
FROM household_members m
         JOIN health_conditions c ON c.code = 'OBE'
WHERE m.display_name = 'Child';

-- =========================
-- INGREDIENTS
-- =========================
INSERT INTO ingredients (name, category, default_unit)
VALUES
    ('Chicken Breast', 'meat', 'g'),
    ('Egg', 'protein', 'piece'),
    ('Milk', 'dairy', 'ml'),
    ('Rice', 'grain', 'g'),
    ('Broccoli', 'vegetable', 'g'),
    ('Peanut', 'nut', 'g');

-- =========================
-- INGREDIENT NUTRITION
-- =========================
INSERT INTO ingredient_nutrition
(ingredient_id, calories_per_100, protein_g_per_100, carb_g_per_100, fat_g_per_100)
SELECT ingredient_id, 165, 31, 0, 3.6 FROM ingredients WHERE name = 'Chicken Breast'
UNION ALL
SELECT ingredient_id, 155, 13, 1.1, 11 FROM ingredients WHERE name = 'Egg'
UNION ALL
SELECT ingredient_id, 42, 3.4, 5, 1 FROM ingredients WHERE name = 'Milk'
UNION ALL
SELECT ingredient_id, 130, 2.7, 28, 0.3 FROM ingredients WHERE name = 'Rice'
UNION ALL
SELECT ingredient_id, 34, 2.8, 7, 0.4 FROM ingredients WHERE name = 'Broccoli'
UNION ALL
SELECT ingredient_id, 567, 26, 16, 49 FROM ingredients WHERE name = 'Peanut';

-- =========================
-- MEMBER ALLERGIES
-- =========================
-- Mother is allergic to Peanut
INSERT INTO member_allergies (member_id, ingredient_id, severity)
SELECT m.member_id, i.ingredient_id, 'severe'
FROM household_members m
         JOIN ingredients i ON i.name = 'Peanut'
WHERE m.display_name = 'Mother';
