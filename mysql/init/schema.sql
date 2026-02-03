-- =========================
-- DATABASE
-- =========================
-- USERS
-- =========================
CREATE TABLE users (
                       user_id VARCHAR(36) PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       full_name VARCHAR(255) NOT NULL,
                       phone VARCHAR(50),
                       avatar_url VARCHAR(512),
                       status ENUM('active', 'blocked') DEFAULT 'active',
                       is_admin BOOLEAN DEFAULT FALSE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


-- =========================
CREATE TABLE family_members (
                                member_id VARCHAR(36) PRIMARY KEY,
                                user_id VARCHAR(36) NOT NULL,

                                display_name VARCHAR(255) NOT NULL,
                                relationship ENUM('self', 'father', 'mother', 'child', 'other') NOT NULL,

                                gender ENUM('male', 'female', 'other'),
                                birth_date DATE,
                                activity_level ENUM('low', 'medium', 'high'),
                                health_notes TEXT,

                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                CONSTRAINT fk_family_member_user
                                    FOREIGN KEY (user_id) REFERENCES users(user_id)
                                        ON DELETE CASCADE
);


-- =========================
-- HEALTH CONDITIONS
-- =========================
CREATE TABLE health_conditions (
                                   condition_id VARCHAR(36) PRIMARY KEY,
                                   code VARCHAR(50) UNIQUE,
                                   name VARCHAR(255) NOT NULL UNIQUE
);


-- =========================
-- MEMBER CONDITIONS (M:N)
-- =========================
CREATE TABLE member_conditions (
                                   member_id VARCHAR(36) NOT NULL,
                                   condition_id VARCHAR(36) NOT NULL,
                                   PRIMARY KEY (member_id, condition_id),

                                   CONSTRAINT fk_mc_member
                                       FOREIGN KEY (member_id) REFERENCES family_members(member_id)
                                           ON DELETE CASCADE,

                                   CONSTRAINT fk_mc_condition
                                       FOREIGN KEY (condition_id) REFERENCES health_conditions(condition_id)
                                           ON DELETE CASCADE
);


-- =========================
-- INGREDIENTS
-- =========================
CREATE TABLE ingredients (
                             ingredient_id VARCHAR(36) PRIMARY KEY,
                             name VARCHAR(255) NOT NULL UNIQUE,
                             category VARCHAR(100),
                             default_unit VARCHAR(50),
                             image_url VARCHAR(512),
                             is_active BOOLEAN DEFAULT TRUE,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


-- =========================
-- MEMBER ALLERGIES
-- =========================
CREATE TABLE member_allergies (
                                  allergy_id VARCHAR(36) PRIMARY KEY,
                                  member_id VARCHAR(36) NOT NULL,
                                  ingredient_id VARCHAR(36) NOT NULL,
                                  severity ENUM('mild', 'medium', 'severe') NOT NULL,

                                  UNIQUE (member_id, ingredient_id),

                                  CONSTRAINT fk_allergy_member
                                      FOREIGN KEY (member_id) REFERENCES family_members(member_id)
                                          ON DELETE CASCADE,

                                  CONSTRAINT fk_allergy_ingredient
                                      FOREIGN KEY (ingredient_id) REFERENCES ingredients(ingredient_id)
                                          ON DELETE CASCADE
);


-- =========================
-- INGREDIENT NUTRITION
-- =========================
CREATE TABLE ingredient_nutrition (
                                      ingredient_id VARCHAR(36) PRIMARY KEY,
                                      calories_per_100 DECIMAL(6,2),
                                      protein_g_per_100 DECIMAL(6,2),
                                      carb_g_per_100 DECIMAL(6,2),
                                      fat_g_per_100 DECIMAL(6,2),

                                      CONSTRAINT fk_nutrition_ingredient
                                          FOREIGN KEY (ingredient_id) REFERENCES ingredients(ingredient_id)
                                              ON DELETE CASCADE
);


-- =========================
-- RECIPES
-- =========================
CREATE TABLE recipes (
                         recipe_id VARCHAR(36) PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         description TEXT,
                         instructions TEXT,
                         prep_time_min INT,
                         cook_time_min INT,
                         base_servings INT DEFAULT 1,
                         image_url VARCHAR(512),

                         created_by VARCHAR(36),
                         status ENUM('draft', 'published', 'archived') DEFAULT 'draft',

                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                         CONSTRAINT fk_recipe_creator
                             FOREIGN KEY (created_by) REFERENCES users(user_id)
                                 ON DELETE SET NULL
);


-- =========================
-- RECIPE INGREDIENTS
-- =========================
CREATE TABLE recipe_ingredients (
                                    recipe_id VARCHAR(36) NOT NULL,
                                    ingredient_id VARCHAR(36) NOT NULL,
                                    quantity DECIMAL(10,2) NOT NULL,
                                    unit VARCHAR(50) NOT NULL,
                                    is_optional BOOLEAN DEFAULT FALSE,

                                    PRIMARY KEY (recipe_id, ingredient_id),

                                    CONSTRAINT fk_ri_recipe
                                        FOREIGN KEY (recipe_id) REFERENCES recipes(recipe_id)
                                            ON DELETE CASCADE,

                                    CONSTRAINT fk_ri_ingredient
                                        FOREIGN KEY (ingredient_id) REFERENCES ingredients(ingredient_id)
                                            ON DELETE CASCADE
);


-- =========================
-- ORDERS
-- =========================
CREATE TABLE orders (
                        order_id VARCHAR(36) PRIMARY KEY,
                        user_id VARCHAR(36) NOT NULL,

                        status ENUM('pending', 'confirmed', 'completed', 'canceled') DEFAULT 'pending',
                        delivery_address_text TEXT NOT NULL,
                        delivery_phone VARCHAR(50),
                        note TEXT,

                        total_amount DECIMAL(12,2),
                        payment_method VARCHAR(50),

                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                        CONSTRAINT fk_order_user
                            FOREIGN KEY (user_id) REFERENCES users(user_id)
                                ON DELETE CASCADE
);


-- =========================
-- ORDER ITEMS
-- =========================
CREATE TABLE order_items (
                             order_item_id VARCHAR(36) PRIMARY KEY,
                             order_id VARCHAR(36) NOT NULL,
                             ingredient_id VARCHAR(36) NOT NULL,

                             quantity DECIMAL(10,2) NOT NULL,
                             unit VARCHAR(50) NOT NULL,
                             price DECIMAL(10,2),
                             line_total DECIMAL(12,2),

                             CONSTRAINT fk_oi_order
                                 FOREIGN KEY (order_id) REFERENCES orders(order_id)
                                     ON DELETE CASCADE,

                             CONSTRAINT fk_oi_ingredient
                                 FOREIGN KEY (ingredient_id) REFERENCES ingredients(ingredient_id)
                                     ON DELETE RESTRICT
);

