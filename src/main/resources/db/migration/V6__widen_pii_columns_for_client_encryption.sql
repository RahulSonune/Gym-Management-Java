-- Client sends FLENC1:-prefixed AES-GCM ciphertext; store opaque strings (no server decrypt).
ALTER TABLE member
    MODIFY member_code VARCHAR(512) NOT NULL,
    MODIFY first_name VARCHAR(512) NOT NULL,
    MODIFY last_name VARCHAR(512) NULL,
    MODIFY gender VARCHAR(512) NULL,
    MODIFY date_of_birth VARCHAR(512) NULL,
    MODIFY email VARCHAR(512) NULL,
    MODIFY phone VARCHAR(512) NOT NULL,
    MODIFY emergency_contact_name VARCHAR(512) NULL,
    MODIFY emergency_contact_phone VARCHAR(512) NULL,
    MODIFY source VARCHAR(512) NULL;

ALTER TABLE membership_plan
    MODIFY name VARCHAR(512) NOT NULL;

ALTER TABLE attendance_log
    MODIFY denied_reason VARCHAR(512) NULL;
