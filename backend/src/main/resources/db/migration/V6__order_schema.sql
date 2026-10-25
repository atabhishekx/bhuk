-- =============================================================
-- V6__order_schema.sql
-- Restru — Order lifecycle, payments, status audit, idempotency
-- Money is stored in integer minor units (paise).
-- Order state and payment state are strictly separated.
-- =============================================================

-- ----- ORDERS -----
CREATE TABLE orders (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    order_number          VARCHAR(32)  NOT NULL,
    customer_id           BIGINT       NOT NULL,
    outlet_id             BIGINT       NOT NULL,
    status                VARCHAR(30)  NOT NULL DEFAULT 'PLACED',
    subtotal_minor        INT          NOT NULL DEFAULT 0,
    tax_minor             INT          NOT NULL DEFAULT 0,
    delivery_fee_minor    INT          NOT NULL DEFAULT 0,
    grand_total_minor     INT          NOT NULL DEFAULT 0,
    delivery_address_json JSON         NOT NULL,
    idempotency_key       VARCHAR(128) NULL,
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_orders_order_number UNIQUE (order_number),
    CONSTRAINT uk_orders_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT fk_orders_outlet FOREIGN KEY (outlet_id) REFERENCES outlets(id),
    CONSTRAINT chk_order_grand_total CHECK (grand_total_minor >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_orders_customer ON orders(customer_id, created_at DESC);
CREATE INDEX idx_orders_outlet_status ON orders(outlet_id, status, created_at DESC);
CREATE INDEX idx_orders_idempotency ON orders(idempotency_key);

-- ----- ORDER ITEMS -----
CREATE TABLE order_items (
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    order_id          BIGINT      NOT NULL,
    item_id           BIGINT      NULL,
    item_name_snapshot VARCHAR(200) NOT NULL,
    quantity          INT         NOT NULL DEFAULT 1,
    unit_price_minor  INT         NOT NULL,
    line_total_minor  INT         NOT NULL,
    instructions      VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_item FOREIGN KEY (item_id) REFERENCES menu_items(id) ON DELETE SET NULL,
    CONSTRAINT chk_order_item_qty CHECK (quantity > 0),
    CONSTRAINT chk_order_item_prices CHECK (unit_price_minor >= 0 AND line_total_minor >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_order_items_order ON order_items(order_id);

-- ----- ORDER ITEM MODIFIERS (snapshot of selected modifiers) -----
CREATE TABLE order_item_modifiers (
    id                 BIGINT   NOT NULL AUTO_INCREMENT,
    order_item_id      BIGINT   NOT NULL,
    name_snapshot      VARCHAR(100) NOT NULL,
    price_delta_minor  INT      NOT NULL DEFAULT 0,
    quantity           INT      NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_item_modifiers_order_item FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE,
    CONSTRAINT chk_order_item_modifier_qty CHECK (quantity > 0),
    CONSTRAINT chk_order_item_modifier_price CHECK (price_delta_minor >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_order_item_modifiers_order_item ON order_item_modifiers(order_item_id);

-- ----- ORDER STATUS EVENTS (append-only audit) -----
CREATE TABLE order_status_events (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    order_id   BIGINT      NOT NULL,
    old_status VARCHAR(30) NULL,
    new_status VARCHAR(30) NOT NULL,
    actor_id   BIGINT      NULL,
    actor_role VARCHAR(30) NULL,
    reason     VARCHAR(255) NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_status_events_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_status_events_actor FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_order_status_events_order ON order_status_events(order_id, created_at);

-- ----- PAYMENTS (separate from order state) -----
CREATE TABLE payments (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    order_id          BIGINT       NULL,
    provider          VARCHAR(30)  NOT NULL DEFAULT 'razorpay',
    provider_reference VARCHAR(128) NULL,
    amount_minor      INT          NOT NULL,
    status            VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    method            VARCHAR(50)  NULL,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT uk_payments_provider_reference UNIQUE (provider, provider_reference),
    CONSTRAINT chk_payment_amount CHECK (amount_minor >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_payments_order ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);

-- ----- REFUNDS -----
CREATE TABLE refunds (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    payment_id         BIGINT       NOT NULL,
    provider_reference VARCHAR(128) NULL,
    amount_minor       INT          NOT NULL,
    status             VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    reason             VARCHAR(255) NULL,
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_refunds_payment FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE,
    CONSTRAINT chk_refund_amount CHECK (amount_minor >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----- IDEMPOTENCY RECORDS -----
CREATE TABLE idempotency_records (
    record_key        VARCHAR(128) NOT NULL,
    actor_id        BIGINT       NULL,
    operation       VARCHAR(60)  NOT NULL,
    request_hash    VARCHAR(128) NOT NULL,
    response_status INT          NULL,
    response_body   JSON         NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at      TIMESTAMP    NULL,
    PRIMARY KEY (record_key),
    CONSTRAINT fk_idempotency_actor FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_idempotency_expires ON idempotency_records(expires_at);
