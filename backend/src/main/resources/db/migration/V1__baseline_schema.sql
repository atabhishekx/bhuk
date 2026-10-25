-- =============================================================
-- V1__baseline_schema.sql
-- Restru Food Delivery System — Initial database schema
-- =============================================================
-- Covers: users, roles, outlets, outlet_hours
-- Money is stored in integer minor units (paise).
-- Timestamps are UTC.
-- =============================================================

-- ----- USERS -----
CREATE TABLE users (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    name        VARCHAR(150) NOT NULL,
    phone       VARCHAR(20)  NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----- ROLES -----
-- A user may hold multiple roles. outlet_id scopes staff/manager to an outlet.
CREATE TABLE user_roles (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL,
    role       VARCHAR(30) NOT NULL,
    outlet_id  BIGINT      NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role_outlet (user_id, role, outlet_id),
    CONSTRAINT fk_userroles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----- OUTLETS -----
CREATE TABLE outlets (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    name         VARCHAR(200) NOT NULL,
    address      TEXT         NOT NULL,
    city         VARCHAR(100) NULL,
    state        VARCHAR(100) NULL,
    pincode      VARCHAR(10)  NULL,
    latitude     DECIMAL(10,7) NULL,
    longitude    DECIMAL(10,7) NULL,
    timezone     VARCHAR(50)  NOT NULL DEFAULT 'Asia/Kolkata',
    phone        VARCHAR(20)  NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add the foreign key from user_roles to outlets now that outlets exists
ALTER TABLE user_roles
    ADD CONSTRAINT fk_userroles_outlet FOREIGN KEY (outlet_id) REFERENCES outlets(id) ON DELETE SET NULL;

-- ----- OUTLET HOURS -----
-- weekday: 0 = Monday … 6 = Sunday (ISO 8601)
CREATE TABLE outlet_hours (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    outlet_id  BIGINT      NOT NULL,
    weekday    TINYINT     NOT NULL,
    open_time  TIME        NOT NULL,
    close_time TIME        NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_outlet_weekday (outlet_id, weekday),
    CONSTRAINT fk_outlethours_outlet FOREIGN KEY (outlet_id) REFERENCES outlets(id) ON DELETE CASCADE,
    CONSTRAINT chk_weekday CHECK (weekday BETWEEN 0 AND 6),
    CONSTRAINT chk_hours CHECK (open_time < close_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

