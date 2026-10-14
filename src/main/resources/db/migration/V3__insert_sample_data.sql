-- ===================================================================
-- Flyway Migration V3: Insert Demo User and Initial Sample Records
-- ===================================================================

-- Insert Default Demo User
-- Credentials: demo@grocery.com / password123
INSERT INTO users (id, full_name, email, password_hash, created_at, updated_at)
VALUES (
    1,
    'Demo User',
    'demo@grocery.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', -- BCrypt hash of 'password123'
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);

-- Insert Sample Grocery Items for Demo User (user_id = 1)
INSERT INTO grocery_items (id, user_id, category_id, name, unit, minimum_stock, estimated_unit_price, notes, created_at, updated_at)
VALUES
(1, 1, (SELECT id FROM grocery_categories WHERE name = 'Grains'), 'Basmati Rice', 'kg', 2.00, 80.00, 'Premium long grain rice', NOW(), NOW()),
(2, 1, (SELECT id FROM grocery_categories WHERE name = 'Dairy'), 'Whole Milk', 'litre', 2.00, 65.00, 'Fresh pasteurized whole milk', NOW(), NOW()),
(3, 1, (SELECT id FROM grocery_categories WHERE name = 'Vegetables'), 'Fresh Tomatoes', 'kg', 1.00, 40.00, 'Farm fresh red tomatoes', NOW(), NOW()),
(4, 1, (SELECT id FROM grocery_categories WHERE name = 'Vegetables'), 'Red Onions', 'kg', 1.50, 35.00, 'Medium dry onions', NOW(), NOW()),
(5, 1, (SELECT id FROM grocery_categories WHERE name = 'Meat and Fish'), 'Chicken Breast', 'kg', 1.00, 240.00, 'Boneless skinless chicken breast', NOW(), NOW()),
(6, 1, (SELECT id FROM grocery_categories WHERE name = 'Spices'), 'Olive Oil', 'litre', 1.00, 650.00, 'Extra virgin cold-pressed olive oil', NOW(), NOW()),
(7, 1, (SELECT id FROM grocery_categories WHERE name = 'Bakery'), 'Whole Wheat Bread', 'piece', 1.00, 45.00, 'Daily fresh sliced bread', NOW(), NOW()),
(8, 1, (SELECT id FROM grocery_categories WHERE name = 'Fruits'), 'Royal Gala Apples', 'kg', 1.50, 150.00, 'Crisp red apples', NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Insert Inventory Batches
-- 1. Basmati Rice: plenty of stock (5.00 kg)
INSERT INTO inventory_batches (id, grocery_item_id, quantity_received, quantity_remaining, purchase_date, expiry_date, created_at, updated_at)
VALUES (1, 1, 5.00, 5.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 180 DAY), NOW(), NOW())
ON DUPLICATE KEY UPDATE quantity_remaining = VALUES(quantity_remaining);

-- 2. Whole Milk: 0.50 litre remaining (Near Expiry in 2 days)
INSERT INTO inventory_batches (id, grocery_item_id, quantity_received, quantity_remaining, purchase_date, expiry_date, created_at, updated_at)
VALUES (2, 2, 2.00, 0.50, DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_ADD(CURDATE(), INTERVAL 2 DAY), NOW(), NOW())
ON DUPLICATE KEY UPDATE quantity_remaining = VALUES(quantity_remaining);

-- 3. Fresh Tomatoes: 0.50 kg remaining (Below minimum stock of 1.00 kg -> Low Stock)
INSERT INTO inventory_batches (id, grocery_item_id, quantity_received, quantity_remaining, purchase_date, expiry_date, created_at, updated_at)
VALUES (3, 3, 2.00, 0.50, DATE_SUB(CURDATE(), INTERVAL 4 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY), NOW(), NOW())
ON DUPLICATE KEY UPDATE quantity_remaining = VALUES(quantity_remaining);

-- 4. Whole Wheat Bread: Expired batch for demonstration
INSERT INTO inventory_batches (id, grocery_item_id, quantity_received, quantity_remaining, purchase_date, expiry_date, created_at, updated_at)
VALUES (4, 7, 1.00, 1.00, DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_SUB(CURDATE(), INTERVAL 2 DAY), NOW(), NOW())
ON DUPLICATE KEY UPDATE quantity_remaining = VALUES(quantity_remaining);

-- 5. Chicken Breast: 1.50 kg remaining, fresh
INSERT INTO inventory_batches (id, grocery_item_id, quantity_received, quantity_remaining, purchase_date, expiry_date, created_at, updated_at)
VALUES (5, 5, 2.00, 1.50, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 14 DAY), NOW(), NOW())
ON DUPLICATE KEY UPDATE quantity_remaining = VALUES(quantity_remaining);

-- Insert Sample Recipe
INSERT INTO recipes (id, user_id, name, description, default_servings, preparation_instructions, created_at, updated_at)
VALUES (
    1,
    1,
    'Chicken Fried Rice',
    'Flavorful homemade chicken stir-fried rice with onions and spices',
    4,
    '1. Cook basmati rice and let it cool completely.\n2. Heat olive oil in a wok and sauté chopped red onions until translucent.\n3. Add diced chicken breast and cook until golden brown.\n4. Toss in cooled rice with seasoning.\n5. Garnish and serve hot for 4 persons.',
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Insert Recipe Ingredients
INSERT INTO recipe_ingredients (id, recipe_id, grocery_item_id, quantity_required, unit)
VALUES
(1, 1, 1, 0.40, 'kg'),  -- 400 g Rice
(2, 1, 5, 0.35, 'kg'),  -- 350 g Chicken
(3, 1, 4, 0.15, 'kg'),  -- 150 g Onion
(4, 1, 6, 0.05, 'litre') -- 50 ml Olive Oil
ON DUPLICATE KEY UPDATE quantity_required = VALUES(quantity_required);

-- Insert Sample Meal Plan for Current Week
INSERT INTO meal_plans (id, user_id, name, week_start_date, week_end_date, created_at, updated_at)
VALUES (
    1,
    1,
    'Current Week Family Plan',
    CURDATE(),
    DATE_ADD(CURDATE(), INTERVAL 6 DAY),
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Insert Meal Plan Entry
INSERT INTO meal_plan_entries (id, meal_plan_id, recipe_id, planned_date, meal_type, servings, preparation_status)
VALUES (
    1,
    1,
    1,
    CURDATE(),
    'DINNER',
    4,
    'PLANNED'
) ON DUPLICATE KEY UPDATE servings = VALUES(servings);

-- Insert Monthly Budget for Current Month and Year
INSERT INTO budgets (id, user_id, budget_month, budget_year, budget_amount, created_at, updated_at)
VALUES (
    1,
    1,
    MONTH(CURDATE()),
    YEAR(CURDATE()),
    5000.00,
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE budget_amount = VALUES(budget_amount);

-- Insert Sample Recorded Expense
INSERT INTO expenses (id, user_id, shopping_list_item_id, amount, expense_date, description, created_at)
VALUES (
    1,
    1,
    NULL,
    850.00,
    CURDATE(),
    'Supermarket weekend restock (Milk, Eggs, Bread)',
    NOW()
) ON DUPLICATE KEY UPDATE amount = VALUES(amount);
