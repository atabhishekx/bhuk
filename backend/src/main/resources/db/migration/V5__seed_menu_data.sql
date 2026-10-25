-- =============================================================
-- V5__seed_menu_data.sql
-- Restru — Development seed menu data (synthetic only).
-- =============================================================

-- Seed menu items (base_price_minor in paise; e.g. 24900 = ₹249.00)
-- Categories removed: Starters, Main Course, Beverages, Desserts (no images)

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

-- Link items to modifier groups (no items in seed)

-- Outlet item availability (no items in seed)
