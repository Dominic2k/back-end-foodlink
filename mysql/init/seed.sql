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
-- DISH CATEGORIES
-- =========================
INSERT INTO dish_categories (category_id, name, description, is_active)
VALUES
    ('da100000-0000-0000-0000-000000000001', 'Breakfast', 'Morning dishes and light meals', 1),
    ('da100000-0000-0000-0000-000000000002', 'Lunch', 'Main dishes for noon meals', 1),
    ('da100000-0000-0000-0000-000000000003', 'Dinner', 'Evening dishes for family meals', 1),
    ('da100000-0000-0000-0000-000000000004', 'Snack', 'Quick small portions between meals', 1);

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
INSERT INTO ingredients (ingredient_id, name, category, base_unit, price_per_base_unit, stock_quantity_base)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'Chicken Breast', 'meat', 'g', 120.0000, 5000.000),
    ('22222222-2222-2222-2222-222222222222', 'Egg', 'protein', 'piece', 3000.0000, 200.000),
    ('33333333-3333-3333-3333-333333333333', 'Milk', 'dairy', 'ml', 25.0000, 10000.000),
    ('44444444-4444-4444-4444-444444444444', 'Rice', 'grain', 'g', 25.0000, 20000.000),
    ('55555555-5555-5555-5555-555555555555', 'Broccoli', 'vegetable', 'g', 80.0000, 8000.000),
    ('66666666-6666-6666-6666-666666666666', 'Peanut', 'nut', 'g', 100.0000, 3000.000);

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

-- =========================
-- RECIPES (PUBLISHED FOR AI RECOMMENDATION)
-- =========================
INSERT INTO recipes (
    recipe_id, name, description, instructions, prep_time_min, cook_time_min, base_servings, created_by, status
)
VALUES
    (
        '88888888-8888-8888-8888-888888888888',
        'Boiled Chicken Broccoli Bowl',
        'Lean chicken with broccoli, low oil and balanced protein.',
        '1) Boil chicken breast and slice. 2) Steam broccoli. 3) Serve with a small rice portion.',
        10,
        20,
        1,
        'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        'published'
    ),
    (
        '99999999-9999-9999-9999-999999999999',
        'Peanut Fried Rice',
        'High-energy fried rice with crushed peanuts.',
        '1) Cook rice. 2) Stir-fry with egg. 3) Add crushed peanuts and serve.',
        10,
        15,
        1,
        'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        'published'
    ),
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        'Milk Egg Scramble',
        'Soft scrambled eggs cooked with a little milk.',
        '1) Beat eggs with milk. 2) Cook on low heat until creamy.',
        5,
        8,
        1,
        'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        'published'
    ),
    (
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        'Steamed Broccoli Egg Bowl',
        'Simple steamed broccoli with boiled egg, suitable for light meals.',
        '1) Steam broccoli. 2) Boil egg. 3) Slice egg and serve together.',
        8,
        10,
        1,
        'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        'published'
    ),
    (
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        'Chicken Rice Plate',
        'Balanced chicken and rice plate for everyday meal.',
        '1) Pan-sear chicken breast with little oil. 2) Steam rice. 3) Serve hot.',
        12,
        18,
        1,
        'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        'published'
    );

-- =========================
-- RECIPE INGREDIENTS
-- =========================
INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity, unit, is_optional)
VALUES
    -- Boiled Chicken Broccoli Bowl
    ('88888888-8888-8888-8888-888888888888', '11111111-1111-1111-1111-111111111111', 150, 'g', 0),
    ('88888888-8888-8888-8888-888888888888', '55555555-5555-5555-5555-555555555555', 120, 'g', 0),
    ('88888888-8888-8888-8888-888888888888', '44444444-4444-4444-4444-444444444444', 80, 'g', 1),

    -- Peanut Fried Rice
    ('99999999-9999-9999-9999-999999999999', '44444444-4444-4444-4444-444444444444', 180, 'g', 0),
    ('99999999-9999-9999-9999-999999999999', '22222222-2222-2222-2222-222222222222', 1, 'piece', 0),
    ('99999999-9999-9999-9999-999999999999', '66666666-6666-6666-6666-666666666666', 30, 'g', 0),

    -- Milk Egg Scramble
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '22222222-2222-2222-2222-222222222222', 2, 'piece', 0),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '33333333-3333-3333-3333-333333333333', 80, 'ml', 0),

    -- Steamed Broccoli Egg Bowl
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '55555555-5555-5555-5555-555555555555', 150, 'g', 0),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222', 1, 'piece', 0),

    -- Chicken Rice Plate
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', '11111111-1111-1111-1111-111111111111', 140, 'g', 0),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', '44444444-4444-4444-4444-444444444444', 160, 'g', 0);

-- Insert initial Health Conditions with detailed info
INSERT INTO health_conditions (condition_id, code, name, description, dietary_advice, exercise_advice, image_url) VALUES
(UUID(), 'CELIAC', 'Celiac Disease', 'Một bệnh tự miễn dịch mà việc tiêu thụ gluten dẫn đến tổn thương ở ruột non.', 'Tuyệt đối không ăn thực phẩm chứa gluten (lúa mì, lúa mạch). Ưu tiên gạo, ngô, khoai tây và các sản phẩm dán nhãn Gluten-free.', 'Duy trì các bài tập cardio nhẹ nhàng và yoga để cải thiện sức khỏe tiêu hóa và giảm căng thẳng.', 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c'),
(UUID(), 'DIABETES', 'Diabetes', 'Bệnh lý đặc trưng bởi lượng đường trong máu cao kéo dài.', 'Hạn chế tinh bột tinh chế và đường. Tăng cường chất xơ từ rau xanh, hạt ngũ cốc nguyên cám. Chia nhỏ bữa ăn trong ngày.', 'Đi bộ nhanh ít nhất 30 phút mỗi ngày. Các bài tập rèn luyện sức bền giúp cải thiện độ nhạy insulin.', 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd'),
(UUID(), 'ALLERGY', 'Food Allergy', 'Phản ứng miễn dịch bất thường của cơ thể đối với một số loại thực phẩm nhất định.', 'Xác định chính xác tác nhân gây dị ứng thông qua xét nghiệm. Luôn kiểm tra kỹ nhãn mác thực phẩm trước khi dùng.', 'Không có bài tập cụ thể, nhưng duy trì sức khỏe tổng thể giúp cơ thể hồi phục nhanh hơn sau các phản ứng nhẹ.', 'https://images.unsplash.com/photo-1490645935967-10de6ba17061'),
(UUID(), 'HYPERTENSION', 'Hypertension', 'Tình trạng áp lực máu đẩy vào thành động mạch quá cao (Cao huyết áp).', 'Hạn chế muối (dưới 5g/ngày). Áp dụng chế độ ăn DASH: nhiều trái cây, rau quả, các sản phẩm sữa ít béo.', 'Các bài tập aerobic như bơi lội, đạp xe đặc biệt tốt cho sức khỏe tim mạch và giúp hạ huyết áp.', 'https://images.unsplash.com/photo-14666323470c7-99bd19391068'),
(UUID(), 'OBESITY', 'Obesity', 'Tình trạng tích tụ mỡ quá mức gây ảnh hưởng xấu đến sức khỏe.', 'Giảm lượng calo nạp vào, tăng cường protein và rau xanh. Tránh thức ăn nhanh, đồ uống có đường và thực phẩm chế biến sẵn.', 'Kết hợp rèn luyện sức mạnh (Weights) và cardio. Đặt mục tiêu vận động ít nhất 150 phút/tuần.', 'https://images.unsplash.com/photo-1505253716362-afaea1d3d1af');
