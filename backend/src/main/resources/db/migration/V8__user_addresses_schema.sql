-- =============================================================
-- V8__user_addresses_schema.sql
-- Restru — Customer saved addresses
-- =============================================================

CREATE TABLE user_addresses (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    user_id       BIGINT       NOT NULL,
    full_address  TEXT         NOT NULL,
    city          VARCHAR(100) NULL,
    state         VARCHAR(100) NULL,
    pincode       VARCHAR(20)  NULL,
    landmark      VARCHAR(100) NULL,
    phone         VARCHAR(20)  NULL,
    is_default    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_addresses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_user_addresses_user ON user_addresses(user_id);
CREATE INDEX idx_user_addresses_default ON user_addresses(user_id, is_default);
