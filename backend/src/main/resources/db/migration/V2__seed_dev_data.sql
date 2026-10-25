-- =============================================================
-- V2__seed_dev_data.sql
-- Development seed data — synthetic only, no real credentials.
-- =============================================================

-- Seed one outlet (single-outlet MVP)
INSERT INTO outlets (name, address, city, state, pincode, phone, status)
VALUES ('Restru Main Branch', '123 Food Street, Koramangala', 'Bengaluru', 'Karnataka', '560034', '+91-80-12345678', 'ACTIVE');

-- Seed outlet hours (Mon-Sun, 10:00-22:00)
INSERT INTO outlet_hours (outlet_id, weekday, open_time, close_time) VALUES
(1, 0, '10:00:00', '22:00:00'),
(1, 1, '10:00:00', '22:00:00'),
(1, 2, '10:00:00', '22:00:00'),
(1, 3, '10:00:00', '22:00:00'),
(1, 4, '10:00:00', '22:00:00'),
(1, 5, '10:00:00', '22:00:00'),
(1, 6, '10:00:00', '22:00:00');

-- Seed admin user (password = 'admin123' hashed with BCrypt)
-- $2a$10$N9qo8uLOickgx2ZMRZoMye is a BCrypt hash of 'admin123'
INSERT INTO users (email, password, name, phone, status)
VALUES ('admin@restru.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZNhLPBNeDgehXOelGXBh4Y3CARzW.', 'System Admin', '+91-9999999999', 'ACTIVE');

-- Grant ADMIN role (not scoped to outlet — chain-wide)
INSERT INTO user_roles (user_id, role, outlet_id)
VALUES (1, 'ADMIN', NULL);

-- Seed a delivery-guy user (password = 'password123', same BCrypt hash as admin seed)
INSERT INTO users (email, password, name, phone, status)
VALUES ('delivery@restru.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZNhLPBNeDgehXOelGXBh4Y3CARzW.', 'Delivery Partner', '+91-9999999998', 'ACTIVE');

INSERT INTO user_roles (user_id, role, outlet_id)
VALUES (2, 'DELIVERY_GUY', 1);

-- Seed a regular customer user
INSERT INTO users (email, password, name, phone, status)
VALUES ('customer@restru.local', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZNhLPBNeDgehXOelGXBh4Y3CARzW.', 'Customer User', '+91-9999999997', 'ACTIVE');

INSERT INTO user_roles (user_id, role, outlet_id)
VALUES (3, 'USER', NULL);

