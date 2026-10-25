-- =============================================================
-- V3__menu_schema.sql
-- Restru — Menu categories, items, modifiers, availability
-- Money is stored in integer minor units (paise).
-- =============================================================

-- ----- MENU CATEGORIES -----
CREATE TABLE menu_categories (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    outlet_id    BIGINT       NULL,
    name         VARCHAR(100) NOT NULL,
    description  TEXT         NULL,
    display_order INT         NOT NULL DEFAULT 0,
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_categories_outlet FOREIGN KEY (outlet_id) REFERENCES outlets(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----- MENU ITEMS -----
CREATE TABLE menu_items (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    category_id    BIGINT       NOT NULL,
    name           VARCHAR(150) NOT NULL,
    description    TEXT         NULL,
    base_price_minor INT        NOT NULL,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    available      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_items_category FOREIGN KEY (category_id) REFERENCES menu_categories(id) ON DELETE CASCADE,
    CONSTRAINT chk_item_price CHECK (base_price_minor >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_menu_items_category_active ON menu_items(category_id, active);

-- ----- MODIFIER GROUPS -----
CREATE TABLE modifier_groups (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    outlet_id   BIGINT      NULL,
    name        VARCHAR(100) NOT NULL,
    min_select  INT         NOT NULL DEFAULT 0,
    max_select  INT         NOT NULL DEFAULT 0,
    required    BOOLEAN     NOT NULL DEFAULT FALSE,
    active      BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_modifier_groups_outlet FOREIGN KEY (outlet_id) REFERENCES outlets(id) ON DELETE SET NULL,
    CONSTRAINT chk_modifier_select CHECK (min_select <= max_select)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----- MODIFIER OPTIONS -----
CREATE TABLE modifier_options (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    group_id        BIGINT      NOT NULL,
    name            VARCHAR(100) NOT NULL,
    price_delta_minor INT       NOT NULL DEFAULT 0,
    active          BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_modifier_options_group FOREIGN KEY (group_id) REFERENCES modifier_groups(id) ON DELETE CASCADE,
    CONSTRAINT chk_modifier_option_price CHECK (price_delta_minor >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_modifier_options_group_active ON modifier_options(group_id, active);

-- ----- ITEM ↔ MODIFIER GROUPS (many-to-many) -----
CREATE TABLE item_modifier_groups (
    item_id         BIGINT NOT NULL,
    modifier_group_id BIGINT NOT NULL,
    PRIMARY KEY (item_id, modifier_group_id),
    CONSTRAINT fk_img_item FOREIGN KEY (item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
    CONSTRAINT fk_img_modifier_group FOREIGN KEY (modifier_group_id) REFERENCES modifier_groups(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----- OUTLET ITEM AVAILABILITY (optional outlet-specific overrides) -----
CREATE TABLE outlet_item_availability (
    outlet_id          BIGINT   NOT NULL,
    item_id            BIGINT   NOT NULL,
    available          BOOLEAN  NOT NULL DEFAULT TRUE,
    price_override_minor INT    NULL,
    PRIMARY KEY (outlet_id, item_id),
    CONSTRAINT fk_oia_outlet FOREIGN KEY (outlet_id) REFERENCES outlets(id) ON DELETE CASCADE,
    CONSTRAINT fk_oia_item FOREIGN KEY (item_id) REFERENCES menu_items(id) ON DELETE CASCADE,
    CONSTRAINT chk_oia_price CHECK (price_override_minor IS NULL OR price_override_minor >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
