-- =============================================================
-- V4__cart_schema.sql
-- Restru — Shopping cart (one outlet per cart)
-- Money is stored in integer minor units (paise).
-- =============================================================

-- ----- CARTS -----
CREATE TABLE carts (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    customer_id BIGINT       NOT NULL,
    outlet_id   BIGINT       NULL,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_carts_customer FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_carts_outlet FOREIGN KEY (outlet_id) REFERENCES outlets(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE UNIQUE INDEX uk_carts_customer ON carts(customer_id);

-- ----- CART ITEMS -----
CREATE TABLE cart_items (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    cart_id       BIGINT      NOT NULL,
    item_id       BIGINT      NOT NULL,
    quantity      INT         NOT NULL DEFAULT 1,
    instructions  VARCHAR(255) NULL,
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_item FOREIGN KEY (item_id) REFERENCES menu_items(id),
    CONSTRAINT chk_cart_item_qty CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);

-- ----- CART ITEM MODIFIERS (normalized selected modifiers) -----
CREATE TABLE cart_item_modifiers (
    id               BIGINT   NOT NULL AUTO_INCREMENT,
    cart_item_id     BIGINT   NOT NULL,
    modifier_option_id BIGINT NOT NULL,
    quantity         INT      NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    CONSTRAINT fk_cim_cart_item FOREIGN KEY (cart_item_id) REFERENCES cart_items(id) ON DELETE CASCADE,
    CONSTRAINT fk_cim_modifier_option FOREIGN KEY (modifier_option_id) REFERENCES modifier_options(id),
    CONSTRAINT uk_cim_item_option UNIQUE (cart_item_id, modifier_option_id),
    CONSTRAINT chk_cim_qty CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
