-- =============================================================
-- V7__delivery_schema.sql
-- Restru — Delivery partner, assignment, and tracking schema
-- =============================================================

-- ----- DELIVERY PARTNERS -----
CREATE TABLE delivery_partners (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    user_id           BIGINT       NOT NULL,
    outlet_id         BIGINT       NULL,
    status            VARCHAR(30)  NOT NULL DEFAULT 'OFFLINE',
    current_lat       DECIMAL(10,7) NULL,
    current_lng       DECIMAL(10,7) NULL,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_delivery_partners_user UNIQUE (user_id),
    CONSTRAINT fk_delivery_partners_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_delivery_partners_outlet FOREIGN KEY (outlet_id) REFERENCES outlets(id) ON DELETE SET NULL,
    CONSTRAINT chk_delivery_partner_status CHECK (status IN ('ONLINE','OFFLINE','BUSY'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_delivery_partners_status ON delivery_partners(status, outlet_id);

-- ----- DELIVERY ASSIGNMENTS -----
CREATE TABLE delivery_assignments (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    order_id           BIGINT       NOT NULL,
    partner_id         BIGINT       NULL,
    state              VARCHAR(30)  NOT NULL DEFAULT 'UNASSIGNED',
    offered_at         TIMESTAMP    NULL,
    assigned_at        TIMESTAMP    NULL,
    accepted_at        TIMESTAMP    NULL,
    picked_up_at       TIMESTAMP    NULL,
    completed_at       TIMESTAMP    NULL,
    rejection_reason   VARCHAR(255) NULL,
    failure_reason     VARCHAR(255) NULL,
    notes              VARCHAR(1000) NULL,
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_delivery_assignments_order UNIQUE (order_id),
    CONSTRAINT fk_delivery_assignments_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_delivery_assignments_partner FOREIGN KEY (partner_id) REFERENCES delivery_partners(id) ON DELETE SET NULL,
    CONSTRAINT chk_delivery_assignment_state CHECK (state IN ('UNASSIGNED','OFFERED','ASSIGNED','ACCEPTED','PICKED_UP','COMPLETED','DECLINED','EXPIRED','FAILED','CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_delivery_assignments_order ON delivery_assignments(order_id);
CREATE INDEX idx_delivery_assignments_partner ON delivery_assignments(partner_id, state);
CREATE INDEX idx_delivery_assignments_state ON delivery_assignments(state, created_at);

-- ----- DELIVERY TRACKING (location pings) -----
CREATE TABLE delivery_tracking (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    assignment_id BIGINT     NOT NULL,
    lat         DECIMAL(10,7) NOT NULL,
    lng         DECIMAL(10,7) NOT NULL,
    accuracy    INT          NULL,
    heading     INT          NULL,
    speed       INT          NULL,
    recorded_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_delivery_tracking_assignment FOREIGN KEY (assignment_id) REFERENCES delivery_assignments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_delivery_tracking_assignment ON delivery_tracking(assignment_id, recorded_at DESC);

-- ----- DELIVERY ISSUES -----
CREATE TABLE delivery_issues (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    assignment_id  BIGINT       NOT NULL,
    issue_type     VARCHAR(50)  NOT NULL,
    description    VARCHAR(1000) NULL,
    reported_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at    TIMESTAMP    NULL,
    resolution     VARCHAR(500) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_delivery_issues_assignment FOREIGN KEY (assignment_id) REFERENCES delivery_assignments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_delivery_issues_assignment ON delivery_issues(assignment_id);
