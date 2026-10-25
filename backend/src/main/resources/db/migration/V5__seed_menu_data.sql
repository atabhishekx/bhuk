-- =============================================================
-- V5__seed_menu_data.sql
-- Restru — Development seed menu data (synthetic only).
-- =============================================================

-- Seed menu categories for outlet 1 (Restru Main Branch)
INSERT INTO menu_categories (outlet_id, name, description, display_order, active) VALUES
(1, 'Starters', 'Appetizers to begin your meal', 1, TRUE),
(1, 'Main Course', 'Hearty mains', 2, TRUE),
(1, 'Beverages', 'Cold and hot drinks', 3, TRUE),
(1, 'Desserts', 'Sweet endings', 4, TRUE);

-- Seed menu items (base_price_minor in paise; e.g. 24900 = ₹249.00)
INSERT INTO menu_items (category_id, name, description, base_price_minor, active, available) VALUES
(1, 'Paneer Tikka', 'Grilled cottage cheese with spices', 24900, TRUE, TRUE),
(1, 'Veg Spring Rolls', 'Crispy vegetable rolls', 19900, TRUE, TRUE),
(2, 'Butter Chicken', 'Creamy tomato-based chicken curry', 34900, TRUE, TRUE),
(2, 'Paneer Butter Masala', 'Cottage cheese in rich gravy', 29900, TRUE, TRUE),
(2, 'Veg Biryani', 'Fragrant basmati rice with vegetables', 24900, TRUE, TRUE),
(3, 'Masala Chai', 'Spiced Indian tea', 4900, TRUE, TRUE),
(3, 'Cold Coffee', 'Iced blended coffee', 9900, TRUE, TRUE),
(4, 'Gulab Jamun', 'Sweet milk dumplings in syrup', 14900, TRUE, TRUE);

-- Seed modifier groups
INSERT INTO modifier_groups (outlet_id, name, min_select, max_select, required, active) VALUES
(1, 'Spice Level', 0, 1, FALSE, TRUE),
(1, 'Add-ons', 0, 2, FALSE, TRUE),
(1, 'Ice Option', 0, 1, FALSE, TRUE);

-- Seed modifier options (price_delta_minor in paise)
INSERT INTO modifier_options (group_id, name, price_delta_minor, active) VALUES
(1, 'Mild', 0, TRUE),
(1, 'Medium', 0, TRUE),
(1, 'Spicy', 0, TRUE),
(2, 'Extra Cheese', 3000, TRUE),
(2, 'Extra Paneer', 4000, TRUE),
(3, 'No Ice', 0, TRUE),
(3, 'Extra Ice', 0, TRUE);

-- Link items to modifier groups
INSERT INTO item_modifier_groups (item_id, modifier_group_id) VALUES
(1, 1),  -- Paneer Tikka -> Spice Level
(2, 1),  -- Veg Spring Rolls -> Spice Level
(3, 1),  -- Butter Chicken -> Spice Level
(3, 2),  -- Butter Chicken -> Add-ons
(4, 1),  -- Paneer Butter Masala -> Spice Level
(4, 2),  -- Paneer Butter Masala -> Add-ons
(7, 3);  -- Cold Coffee -> Ice Option

-- Outlet item availability (all available at outlet 1)
INSERT INTO outlet_item_availability (outlet_id, item_id, available, price_override_minor) VALUES
(1, 1, TRUE, NULL),
(1, 2, TRUE, NULL),
(1, 3, TRUE, NULL),
(1, 4, TRUE, NULL),
(1, 5, TRUE, NULL),
(1, 6, TRUE, NULL),
(1, 7, TRUE, NULL),
(1, 8, TRUE, NULL);
