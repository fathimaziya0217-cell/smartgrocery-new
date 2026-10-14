-- ===================================================================
-- SMART GROCERY INVENTORY & SHOPPING LIST GENERATOR
-- Standalone Sample Data Reference (MySQL 8.x)
-- ===================================================================

USE smart_grocery_db;

-- Insert Standard Grocery Categories
INSERT INTO grocery_categories (name, description, created_at) VALUES
('Fruits', 'Fresh fruits, berries, and citrus products', NOW()),
('Vegetables', 'Fresh and leafy vegetables, tubers, and herbs', NOW()),
('Dairy', 'Milk, cheese, butter, yogurt, and paneer', NOW()),
('Grains', 'Rice, wheat, oats, lentils, pulses, and flour', NOW()),
('Bakery', 'Breads, buns, tortillas, and baked items', NOW()),
('Meat and Fish', 'Chicken, mutton, fish, seafood, and eggs', NOW()),
('Beverages', 'Juices, coffee, tea, and soft drinks', NOW()),
('Snacks', 'Nuts, chips, biscuits, and confectionery', NOW()),
('Spices', 'Cooking spices, salt, seasonings, and condiments', NOW()),
('Household Essentials', 'Cleaning agents, detergents, paper towels, and bags', NOW()),
('Other', 'General miscellaneous household items', NOW())
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- Insert Default Demo User
-- Credentials: demo@grocery.com / password123
INSERT INTO users (id, full_name, email, password_hash, created_at, updated_at)
VALUES (
    1,
    'Demo User',
    'demo@grocery.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);

-- Insert Sample Items for User 1
INSERT INTO grocery_items (id, user_id, category_id, name, unit, minimum_stock, estimated_unit_price, notes, created_at, updated_at)
VALUES
(1, 1, 4, 'Basmati Rice', 'kg', 2.00, 80.00, 'Premium long grain rice', NOW(), NOW()),
(2, 1, 3, 'Whole Milk', 'litre', 2.00, 65.00, 'Fresh pasteurized whole milk', NOW(), NOW()),
(3, 1, 2, 'Fresh Tomatoes', 'kg', 1.00, 40.00, 'Farm fresh red tomatoes', NOW(), NOW()),
(4, 1, 2, 'Red Onions', 'kg', 1.50, 35.00, 'Medium dry onions', NOW(), NOW()),
(5, 1, 6, 'Chicken Breast', 'kg', 1.00, 240.00, 'Boneless skinless chicken breast', NOW(), NOW()),
(6, 1, 9, 'Olive Oil', 'litre', 1.00, 650.00, 'Extra virgin cold-pressed olive oil', NOW(), NOW()),
(7, 1, 5, 'Whole Wheat Bread', 'piece', 1.00, 45.00, 'Daily fresh sliced bread', NOW(), NOW()),
(8, 1, 1, 'Royal Gala Apples', 'kg', 1.50, 150.00, 'Crisp red apples', NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Insert Batches
INSERT INTO inventory_batches (id, grocery_item_id, quantity_received, quantity_remaining, purchase_date, expiry_date, created_at, updated_at)
VALUES
(1, 1, 5.00, 5.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 180 DAY), NOW(), NOW()),
(2, 2, 2.00, 0.50, DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_ADD(CURDATE(), INTERVAL 2 DAY), NOW(), NOW()),
(3, 3, 2.00, 0.50, DATE_SUB(CURDATE(), INTERVAL 4 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY), NOW(), NOW()),
(4, 7, 1.00, 1.00, DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_SUB(CURDATE(), INTERVAL 2 DAY), NOW(), NOW()),
(5, 5, 2.00, 1.50, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 14 DAY), NOW(), NOW())
ON DUPLICATE KEY UPDATE quantity_remaining = VALUES(quantity_remaining);

-- Insert Recipe
INSERT INTO recipes (id, user_id, name, description, default_servings, preparation_instructions, created_at, updated_at)
VALUES (
    1,
    1,
    'Chicken Fried Rice',
    'Flavorful homemade chicken stir-fried rice with onions and spices',
    4,
    '1. Cook basmati rice and let it cool.\n2. Sauté onions in olive oil.\n3. Add chicken and stir fry.\n4. Toss rice and season.',
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Insert Recipe Ingredients
INSERT INTO recipe_ingredients (id, recipe_id, grocery_item_id, quantity_required, unit)
VALUES
(1, 1, 1, 0.40, 'kg'),
(2, 1, 5, 0.35, 'kg'),
(3, 1, 4, 0.15, 'kg'),
(4, 1, 6, 0.05, 'litre')
ON DUPLICATE KEY UPDATE quantity_required = VALUES(quantity_required);

-- Insert Meal Plan
INSERT INTO meal_plans (id, user_id, name, week_start_date, week_end_date, created_at, updated_at)
VALUES (1, 1, 'Current Week Family Plan', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 6 DAY), NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO meal_plan_entries (id, meal_plan_id, recipe_id, planned_date, meal_type, servings, preparation_status)
VALUES (1, 1, 1, CURDATE(), 'DINNER', 4, 'PLANNED')
ON DUPLICATE KEY UPDATE servings = VALUES(servings);

-- Insert Budget & Expense
INSERT INTO budgets (id, user_id, budget_month, budget_year, budget_amount, created_at, updated_at)
VALUES (1, 1, MONTH(CURDATE()), YEAR(CURDATE()), 5000.00, NOW(), NOW())
ON DUPLICATE KEY UPDATE budget_amount = VALUES(budget_amount);

INSERT INTO expenses (id, user_id, shopping_list_item_id, amount, expense_date, description, created_at)
VALUES (1, 1, NULL, 850.00, CURDATE(), 'Supermarket restock', NOW())
ON DUPLICATE KEY UPDATE amount = VALUES(amount);
