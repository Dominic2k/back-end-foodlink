-- =========================
-- DATABASE
-- =========================

-- =========================
-- USERS
-- =========================
CREATE TABLE users (
                       user_id VARCHAR(36) PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       full_name VARCHAR(255) NOT NULL,
                       phone VARCHAR(50),
                       address VARCHAR(512),
                       avatar_url VARCHAR(512),
                       status ENUM('active', 'blocked') DEFAULT 'active',
                       is_admin BOOLEAN DEFAULT FALSE,
                       last_login_at DATETIME NULL,
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
                                height_cm DECIMAL(5,2),
                                weight_kg DECIMAL(5,2),
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
-- DISH CATEGORIES
-- =========================
CREATE TABLE dish_categories (
                                 category_id VARCHAR(36) PRIMARY KEY,
                                 name VARCHAR(255) NOT NULL UNIQUE,
                                 description TEXT,
                                 is_active BOOLEAN DEFAULT TRUE,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
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
                             base_unit VARCHAR(50) NOT NULL,
                             price_per_base_unit DECIMAL(12,4) NOT NULL,
                             stock_quantity_base DECIMAL(12,3) NOT NULL DEFAULT 0,
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
-- RECIPE CATEGORIES (M:N)
-- =========================
CREATE TABLE recipe_categories (
                                   recipe_id VARCHAR(36) NOT NULL,
                                   category_id VARCHAR(36) NOT NULL,
                                   PRIMARY KEY (recipe_id, category_id),

                                   CONSTRAINT fk_rc_recipe
                                       FOREIGN KEY (recipe_id) REFERENCES recipes(recipe_id)
                                           ON DELETE CASCADE,

                                   CONSTRAINT fk_rc_category
                                       FOREIGN KEY (category_id) REFERENCES dish_categories(category_id)
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
                             recipe_id VARCHAR(36) NOT NULL,
                             servings INT NOT NULL,
                             price_per_serving_snapshot DECIMAL(12,2),
                             line_total DECIMAL(12,2),

                             CONSTRAINT fk_oi_order
                                 FOREIGN KEY (order_id) REFERENCES orders(order_id)
                                     ON DELETE CASCADE,

                             CONSTRAINT fk_oi_recipe
                                 FOREIGN KEY (recipe_id) REFERENCES recipes(recipe_id)
                                     ON DELETE RESTRICT
);

CREATE TABLE order_item_ingredients (
                                        order_item_ingredient_id VARCHAR(36) PRIMARY KEY,
                                        order_item_id VARCHAR(36) NOT NULL,
                                        ingredient_id VARCHAR(36) NOT NULL,
                                        ingredient_name_snapshot VARCHAR(255) NOT NULL,
                                        quantity_base DECIMAL(12,3) NOT NULL,
                                        base_unit VARCHAR(50) NOT NULL,
                                        unit_price_snapshot DECIMAL(12,4) NOT NULL,
                                        line_total DECIMAL(12,2) NOT NULL,

                                        CONSTRAINT fk_oii_order_item
                                            FOREIGN KEY (order_item_id) REFERENCES order_items(order_item_id)
                                                ON DELETE CASCADE,

                                        CONSTRAINT fk_oii_ingredient
                                            FOREIGN KEY (ingredient_id) REFERENCES ingredients(ingredient_id)
                                     ON DELETE RESTRICT
);


-- =========================
-- ACTIVITY LOGS
-- =========================
CREATE TABLE activity_logs (
                               log_id VARCHAR(36) PRIMARY KEY,
                               action ENUM('CREATE', 'UPDATE', 'DELETE', 'STATUS_CHANGE', 'VIEW') NOT NULL,
                               entity_type VARCHAR(50) NOT NULL,
                               entity_id VARCHAR(36),
                               description VARCHAR(500),
                               performed_by VARCHAR(255) NOT NULL,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- =========================
-- APP VISITS
-- =========================
CREATE TABLE app_visits (
                            visit_id VARCHAR(36) PRIMARY KEY,
                            user_id VARCHAR(36),
                            visited_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT fk_visit_user
                                FOREIGN KEY (user_id) REFERENCES users(user_id)
                                    ON DELETE SET NULL
);
-- =========================
-- DISH RECOMENDATION
-- =========================
CREATE TABLE dish_recommendations (
    recommendation_id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    recipe_id CHAR(36) NOT NULL,
    score INT NOT NULL,
    is_suitable BIT(1) NOT NULL,
    reason TEXT NULL,
    suggestion TEXT NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,

    CONSTRAINT pk_dish_recommendations PRIMARY KEY (recommendation_id),
    CONSTRAINT uk_recommend_user_recipe UNIQUE (user_id, recipe_id),

    CONSTRAINT fk_dish_recommendations_user
        FOREIGN KEY (user_id) REFERENCES users(user_id),

    CONSTRAINT fk_dish_recommendations_recipe
        FOREIGN KEY (recipe_id) REFERENCES recipes(recipe_id)
);
