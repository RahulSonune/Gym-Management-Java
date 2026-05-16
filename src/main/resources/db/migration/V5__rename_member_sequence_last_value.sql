-- last_value is a MySQL 8 reserved word; Hibernate UPDATE fails without quoting.
-- No-op when V1 already defines seq_value (fresh installs).
SET @rename_sql = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'member_sequence'
       AND COLUMN_NAME = 'last_value') > 0,
    'ALTER TABLE member_sequence CHANGE COLUMN `last_value` seq_value BIGINT NOT NULL DEFAULT 0',
    'SELECT 1');
PREPARE rename_stmt FROM @rename_sql;
EXECUTE rename_stmt;
DEALLOCATE PREPARE rename_stmt;
