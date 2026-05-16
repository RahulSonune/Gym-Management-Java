-- Seed: FitLife Gym demo data (password for all users: password — BCrypt)

INSERT INTO organization (id, name, currency_code, timezone, multi_branch)
VALUES (1, 'FitLife Gym', 'INR', 'Asia/Kolkata', 1);

INSERT INTO branch (id, organization_id, code, name, city, phone, is_default, is_active)
VALUES
    (1, 1, 'BR-001', 'Main Branch', 'Mumbai', '022-12345678', 1, 1),
    (2, 1, 'BR-002', 'Downtown', 'Mumbai', '022-87654321', 0, 1);

INSERT INTO member_sequence (branch_id, seq_value) VALUES (1, 45), (2, 10);

-- BCrypt hash of "password"
INSERT INTO app_user (id, organization_id, email, password_hash, full_name, is_active)
VALUES
    (1, 1, 'admin@gym.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Admin User', 1),
    (12, 1, 'reception@gym.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Priya Sharma', 1);

INSERT INTO user_role (user_id, role) VALUES
    (1, 'SUPER_ADMIN'),
    (1, 'BRANCH_MANAGER'),
    (12, 'RECEPTIONIST'),
    (12, 'BRANCH_MANAGER');

INSERT INTO staff_branch (user_id, branch_id, is_primary) VALUES
    (1, 1, 1),
    (1, 2, 0),
    (12, 1, 1),
    (12, 2, 0);

INSERT INTO membership_plan (id, organization_id, code, name, duration_days, price_amount_minor, max_freeze_days, allows_pt, allows_classes, is_active, sort_order)
VALUES
    (10, 1, 'GOLD-12', 'Gold 12 Months', 365, 1500000, 30, 1, 1, 1, 1),
    (11, 1, 'SILVER-1', 'Silver Monthly', 30, 250000, 7, 0, 1, 1, 2),
    (12, 1, 'PT-10', 'PT Package 10 Sessions', 90, 1200000, 0, 1, 0, 1, 3);

INSERT INTO member (id, organization_id, branch_id, member_code, first_name, last_name, phone, email, status, joined_at, source)
VALUES
    (1001, 1, 1, 'M-2026-00042', 'Rahul', 'Verma', '9876543210', 'rahul@email.com', 'ACTIVE', '2026-01-10', 'WALK_IN'),
    (1002, 1, 1, 'M-2026-00043', 'Anita', 'Desai', '9876501234', NULL, 'ACTIVE', '2026-03-01', 'WALK_IN'),
    (1003, 1, 2, 'M-2026-00044', 'Vikram', 'Singh', '9123456780', NULL, 'EXPIRED', '2025-06-15', 'REFERRAL'),
    (1004, 1, 1, 'M-2026-00045', 'Neha', 'Kapoor', '9988776655', NULL, 'PROSPECT', '2026-05-14', 'WALK_IN');

INSERT INTO subscription (id, member_id, branch_id, plan_id, status, start_date, end_date, sold_by_user_id)
VALUES
    (501, 1001, 1, 10, 'ACTIVE', '2026-01-01', '2026-12-31', 12),
    (502, 1002, 1, 11, 'ACTIVE', '2026-05-01', '2026-05-31', 12);

INSERT INTO invoice (id, organization_id, branch_id, invoice_number, member_id, subscription_id, status, issue_date, due_date, subtotal_minor, tax_minor, total_minor, amount_paid_minor)
VALUES
    (8001, 1, 1, 'INV-B1-2026-00123', 1001, 501, 'PAID', '2026-01-01', '2026-01-01', 1271186, 228814, 1500000, 1500000),
    (8002, 1, 1, 'INV-B1-2026-00145', 1002, 502, 'OVERDUE', '2026-05-01', '2026-05-05', 127118, 22882, 150000, 0);

INSERT INTO payment (id, organization_id, branch_id, member_id, payment_number, amount_minor, method, status, paid_at, received_by_user_id)
VALUES
    (9001, 1, 1, 1001, 'PAY-B1-2026-00089', 1500000, 'UPI', 'SUCCESS', '2026-01-01 10:30:00', 12),
    (9002, 1, 1, 1002, 'PAY-B1-2026-00102', 100000, 'CASH', 'SUCCESS', '2026-05-01 11:00:00', 12);

INSERT INTO attendance_log (id, organization_id, branch_id, member_id, check_in_at, check_out_at, method)
VALUES
    (70001, 1, 1, 1001, '2026-05-15 06:45:00', NULL, 'QR'),
    (70002, 1, 1, 1002, '2026-05-15 07:10:00', '2026-05-15 08:30:00', 'MANUAL');
