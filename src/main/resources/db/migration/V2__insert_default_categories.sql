-- ===================================================================
-- Flyway Migration V2: Insert Default Grocery Categories
-- ===================================================================

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
