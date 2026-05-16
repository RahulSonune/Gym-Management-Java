-- Client may send FLENC1 ciphertext for staff name/phone (opaque at rest on server).
ALTER TABLE app_user
    MODIFY full_name VARCHAR(512) NOT NULL,
    MODIFY phone VARCHAR(512) NULL;
