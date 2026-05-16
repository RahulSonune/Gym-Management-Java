-- BCrypt hash for plaintext "password" (Spring BCryptPasswordEncoder compatible)
UPDATE app_user
SET password_hash = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'
WHERE email IN ('admin@gym.com', 'reception@gym.com');
