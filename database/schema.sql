-- ===================================================================
-- SMART GROCERY INVENTORY & SHOPPING LIST GENERATOR
-- Standalone Database Schema Reference (MySQL 8.x)
-- ===================================================================

CREATE DATABASE IF NOT EXISTS smart_grocery_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE smart_grocery_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Grocery Categories Table
CREATE TABLE IF NOT EXISTS grocery_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Grocery Items Table
CREATE TABLE IF NOT EXISTS grocery_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    unit VARCHAR(30) NOT NULL,
    minimum_stock DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    estimated_unit_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_items_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_items_category FOREIGN KEY (category_id) REFERENCES grocery_categories(id) ON DELETE RESTRICT,
    INDEX idx_items_user (user_id),
    INDEX idx_items_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Inventory Batches Table
CREATE TABLE IF NOT EXISTS inventory_batches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    grocery_item_id BIGINT NOT NULL,
    quantity_received DECIMAL(10,2) NOT NULL,
    quantity_remaining DECIMAL(10,2) NOT NULL,
    purchase_date DATE NOT NULL,
    expiry_date DATE NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_batches_item FOREIGN KEY (grocery_item_id) REFERENCES grocery_items(id) ON DELETE CASCADE,
    INDEX idx_batches_item (grocery_item_id),
    INDEX idx_batches_expiry (expiry_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Recipes Table
CREATE TABLE IF NOT EXISTS recipes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    default_servings INT NOT NULL DEFAULT 2,
    preparation_instructions TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_recipes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_recipes_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Recipe Ingredients Table
CREATE TABLE IF NOT EXISTS recipe_ingredients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    grocery_item_id BIGINT NOT NULL,
    quantity_required DECIMAL(10,2) NOT NULL,
    unit VARCHAR(30) NOT NULL,
    CONSTRAINT fk_recipe_ingredients_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE,
    CONSTRAINT fk_recipe_ingredients_item FOREIGN KEY (grocery_item_id) REFERENCES grocery_items(id) ON DELETE RESTRICT,
    INDEX idx_recipe_ingredients_recipe (recipe_id),
    INDEX idx_recipe_ingredients_item (grocery_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Meal Plans Table
CREATE TABLE IF NOT EXISTS meal_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    week_start_date DATE NOT NULL,
    week_end_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_meal_plans_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_meal_plans_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Meal Plan Entries Table
CREATE TABLE IF NOT EXISTS meal_plan_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meal_plan_id BIGINT NOT NULL,
    recipe_id BIGINT NOT NULL,
    planned_date DATE NOT NULL,
    meal_type VARCHAR(50) NOT NULL,
    servings INT NOT NULL DEFAULT 2,
    preparation_status VARCHAR(50) NOT NULL DEFAULT 'PLANNED',
    CONSTRAINT fk_meal_plan_entries_plan FOREIGN KEY (meal_plan_id) REFERENCES meal_plans(id) ON DELETE CASCADE,
    CONSTRAINT fk_meal_plan_entries_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE RESTRICT,
    INDEX idx_meal_plan_entries_plan (meal_plan_id),
    INDEX idx_meal_plan_entries_recipe (recipe_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Shopping Lists Table
CREATE TABLE IF NOT EXISTS shopping_lists (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_shopping_lists_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_shopping_lists_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Shopping List Items Table
CREATE TABLE IF NOT EXISTS shopping_list_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shopping_list_id BIGINT NOT NULL,
    grocery_item_id BIGINT NOT NULL,
    quantity_required DECIMAL(10,2) NOT NULL,
    unit VARCHAR(30) NOT NULL,
    estimated_unit_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    estimated_total_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    purchased BOOLEAN NOT NULL DEFAULT FALSE,
    quantity_purchased DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_shopping_list_items_list FOREIGN KEY (shopping_list_id) REFERENCES shopping_lists(id) ON DELETE CASCADE,
    CONSTRAINT fk_shopping_list_items_item FOREIGN KEY (grocery_item_id) REFERENCES grocery_items(id) ON DELETE RESTRICT,
    INDEX idx_shopping_list_items_list (shopping_list_id),
    INDEX idx_shopping_list_items_item (grocery_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Budgets Table
CREATE TABLE IF NOT EXISTS budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    budget_month INT NOT NULL,
    budget_year INT NOT NULL,
    budget_amount DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_budgets_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_month_year UNIQUE (user_id, budget_month, budget_year),
    INDEX idx_budgets_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Expenses Table
CREATE TABLE IF NOT EXISTS expenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    shopping_list_item_id BIGINT NULL,
    amount DECIMAL(10,2) NOT NULL,
    expense_date DATE NOT NULL,
    description VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_expenses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_expenses_shopping_item FOREIGN KEY (shopping_list_item_id) REFERENCES shopping_list_items(id) ON DELETE SET NULL,
    INDEX idx_expenses_user (user_id),
    INDEX idx_expenses_date (expense_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. Alerts Table
CREATE TABLE IF NOT EXISTS alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    grocery_item_id BIGINT NULL,
    alert_type VARCHAR(50) NOT NULL,
    message VARCHAR(500) NOT NULL,
    read_status BOOLEAN NOT NULL DEFAULT FALSE,
    resolved BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_alerts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_alerts_item FOREIGN KEY (grocery_item_id) REFERENCES grocery_items(id) ON DELETE CASCADE,
    INDEX idx_alerts_user (user_id),
    INDEX idx_alerts_status (read_status, resolved)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. Scan Imports Table
CREATE TABLE IF NOT EXISTS scan_imports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    import_type VARCHAR(50) NOT NULL,
    processing_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    CONSTRAINT fk_scan_imports_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_scan_imports_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. Scan Import Items Table
CREATE TABLE IF NOT EXISTS scan_import_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scan_import_id BIGINT NOT NULL,
    grocery_item_id BIGINT NULL,
    item_name VARCHAR(150) NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    unit VARCHAR(30) NOT NULL,
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    processing_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_scan_import_items_import FOREIGN KEY (scan_import_id) REFERENCES scan_imports(id) ON DELETE CASCADE,
    CONSTRAINT fk_scan_import_items_item FOREIGN KEY (grocery_item_id) REFERENCES grocery_items(id) ON DELETE SET NULL,
    INDEX idx_scan_import_items_import (scan_import_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
