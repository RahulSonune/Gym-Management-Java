-- Gym Management System - MySQL schema (single & multi-branch)

CREATE TABLE organization (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    legal_name      VARCHAR(200),
    tax_id          VARCHAR(50),
    currency_code   VARCHAR(3) NOT NULL DEFAULT 'INR',
    timezone        VARCHAR(64) NOT NULL DEFAULT 'Asia/Kolkata',
    logo_url        VARCHAR(500),
    multi_branch    TINYINT(1) NOT NULL DEFAULT 0,
    created_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);

CREATE TABLE branch (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    code            VARCHAR(20) NOT NULL,
    name            VARCHAR(200) NOT NULL,
    address_line1   VARCHAR(200),
    city            VARCHAR(100),
    state           VARCHAR(100),
    postal_code     VARCHAR(20),
    country         VARCHAR(2),
    phone           VARCHAR(20),
    email           VARCHAR(150),
    timezone        VARCHAR(64),
    is_default      TINYINT(1) NOT NULL DEFAULT 0,
    is_active       TINYINT(1) NOT NULL DEFAULT 1,
    created_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at      DATETIME(6),
    CONSTRAINT fk_branch_org FOREIGN KEY (organization_id) REFERENCES organization(id),
    UNIQUE KEY uk_branch_org_code (organization_id, code)
);

CREATE TABLE app_user (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    email           VARCHAR(150) NOT NULL,
    phone           VARCHAR(20),
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(150) NOT NULL,
    is_active       TINYINT(1) NOT NULL DEFAULT 1,
    last_login_at   DATETIME(6),
    created_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at      DATETIME(6),
    CONSTRAINT fk_user_org FOREIGN KEY (organization_id) REFERENCES organization(id),
    UNIQUE KEY uk_user_org_email (organization_id, email)
);

CREATE TABLE user_role (
    user_id BIGINT NOT NULL,
    role    VARCHAR(30) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES app_user(id)
);

CREATE TABLE staff_branch (
    user_id     BIGINT NOT NULL,
    branch_id   BIGINT NOT NULL,
    is_primary  TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, branch_id),
    CONSTRAINT fk_staff_branch_user FOREIGN KEY (user_id) REFERENCES app_user(id),
    CONSTRAINT fk_staff_branch_branch FOREIGN KEY (branch_id) REFERENCES branch(id)
);

CREATE TABLE member (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id         BIGINT NOT NULL,
    branch_id               BIGINT NOT NULL,
    member_code             VARCHAR(512) NOT NULL,
    first_name              VARCHAR(512) NOT NULL,
    last_name               VARCHAR(512),
    gender                  VARCHAR(512),
    date_of_birth           VARCHAR(512),
    email                   VARCHAR(512),
    phone                   VARCHAR(512) NOT NULL,
    emergency_contact_name  VARCHAR(512),
    emergency_contact_phone VARCHAR(512),
    status                  VARCHAR(30) NOT NULL DEFAULT 'PROSPECT',
    joined_at               DATE,
    source                  VARCHAR(512),
    created_by_user_id      BIGINT,
    created_at              DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at              DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at              DATETIME(6),
    CONSTRAINT fk_member_org FOREIGN KEY (organization_id) REFERENCES organization(id),
    CONSTRAINT fk_member_branch FOREIGN KEY (branch_id) REFERENCES branch(id),
    UNIQUE KEY uk_member_branch_code (branch_id, member_code),
    KEY idx_member_phone (branch_id, phone),
    KEY idx_member_status (status)
);

CREATE TABLE membership_plan (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id     BIGINT NOT NULL,
    branch_id           BIGINT,
    code                VARCHAR(30) NOT NULL,
    name                VARCHAR(512) NOT NULL,
    description         TEXT,
    duration_days       INT NOT NULL,
    price_amount_minor  BIGINT NOT NULL,
    currency_code       VARCHAR(3) NOT NULL DEFAULT 'INR',
    tax_percent         DECIMAL(5,2) DEFAULT 18.00,
    max_freeze_days     INT NOT NULL DEFAULT 0,
    allows_pt           TINYINT(1) NOT NULL DEFAULT 0,
    allows_classes      TINYINT(1) NOT NULL DEFAULT 1,
    is_active           TINYINT(1) NOT NULL DEFAULT 1,
    sort_order          INT NOT NULL DEFAULT 0,
    created_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_plan_org FOREIGN KEY (organization_id) REFERENCES organization(id),
    CONSTRAINT fk_plan_branch FOREIGN KEY (branch_id) REFERENCES branch(id)
);

CREATE TABLE subscription (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id           BIGINT NOT NULL,
    branch_id           BIGINT NOT NULL,
    plan_id             BIGINT NOT NULL,
    status              VARCHAR(30) NOT NULL,
    start_date          DATE NOT NULL,
    end_date            DATE NOT NULL,
    auto_renew          TINYINT(1) NOT NULL DEFAULT 0,
    sold_by_user_id     BIGINT,
    created_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_sub_member FOREIGN KEY (member_id) REFERENCES member(id),
    CONSTRAINT fk_sub_branch FOREIGN KEY (branch_id) REFERENCES branch(id),
    CONSTRAINT fk_sub_plan FOREIGN KEY (plan_id) REFERENCES membership_plan(id),
    KEY idx_sub_member_status (member_id, status)
);

CREATE TABLE invoice (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id     BIGINT NOT NULL,
    branch_id           BIGINT NOT NULL,
    invoice_number      VARCHAR(50) NOT NULL,
    member_id           BIGINT NOT NULL,
    subscription_id     BIGINT,
    status              VARCHAR(30) NOT NULL,
    issue_date          DATE NOT NULL,
    due_date            DATE NOT NULL,
    subtotal_minor      BIGINT NOT NULL DEFAULT 0,
    tax_minor           BIGINT NOT NULL DEFAULT 0,
    discount_minor      BIGINT NOT NULL DEFAULT 0,
    total_minor         BIGINT NOT NULL DEFAULT 0,
    amount_paid_minor   BIGINT NOT NULL DEFAULT 0,
    currency_code       VARCHAR(3) NOT NULL DEFAULT 'INR',
    created_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_invoice_org FOREIGN KEY (organization_id) REFERENCES organization(id),
    CONSTRAINT fk_invoice_branch FOREIGN KEY (branch_id) REFERENCES branch(id),
    CONSTRAINT fk_invoice_member FOREIGN KEY (member_id) REFERENCES member(id),
    UNIQUE KEY uk_invoice_number (organization_id, invoice_number)
);

CREATE TABLE payment (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id     BIGINT NOT NULL,
    branch_id           BIGINT NOT NULL,
    member_id           BIGINT NOT NULL,
    payment_number      VARCHAR(50) NOT NULL,
    amount_minor        BIGINT NOT NULL,
    currency_code       VARCHAR(3) NOT NULL DEFAULT 'INR',
    method              VARCHAR(30) NOT NULL,
    status              VARCHAR(30) NOT NULL,
    idempotency_key     VARCHAR(64),
    paid_at             DATETIME(6),
    notes               TEXT,
    received_by_user_id BIGINT,
    created_at          DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_payment_org FOREIGN KEY (organization_id) REFERENCES organization(id),
    CONSTRAINT fk_payment_branch FOREIGN KEY (branch_id) REFERENCES branch(id),
    CONSTRAINT fk_payment_member FOREIGN KEY (member_id) REFERENCES member(id),
    UNIQUE KEY uk_payment_number (organization_id, payment_number),
    UNIQUE KEY uk_idempotency (idempotency_key)
);

CREATE TABLE attendance_log (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    branch_id       BIGINT NOT NULL,
    member_id       BIGINT NOT NULL,
    check_in_at     DATETIME(6) NOT NULL,
    check_out_at    DATETIME(6),
    method          VARCHAR(30) NOT NULL,
    device_id       VARCHAR(100),
    subscription_id BIGINT,
    denied_reason   VARCHAR(512),
    created_by_user_id BIGINT,
    created_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_att_org FOREIGN KEY (organization_id) REFERENCES organization(id),
    CONSTRAINT fk_att_branch FOREIGN KEY (branch_id) REFERENCES branch(id),
    CONSTRAINT fk_att_member FOREIGN KEY (member_id) REFERENCES member(id),
    KEY idx_att_branch_date (branch_id, check_in_at)
);

CREATE TABLE member_sequence (
    branch_id   BIGINT NOT NULL PRIMARY KEY,
    seq_value   BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_seq_branch FOREIGN KEY (branch_id) REFERENCES branch(id)
);
