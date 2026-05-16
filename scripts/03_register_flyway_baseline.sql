-- Register Flyway migrations after manual DB setup (so Spring Boot starts cleanly)
USE gym_management;

CREATE TABLE IF NOT EXISTS flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_time INT NOT NULL,
    success TINYINT(1) NOT NULL,
    PRIMARY KEY (installed_rank)
) ENGINE=InnoDB;

DELETE FROM flyway_schema_history;

INSERT INTO flyway_schema_history
    (installed_rank, version, description, type, script, checksum, installed_by, execution_time, success)
VALUES
    (1, '1', 'schema', 'SQL', 'V1__schema.sql', NULL, 'manual-setup', 0, 1),
    (2, '2', 'seed data', 'SQL', 'V2__seed_data.sql', NULL, 'manual-setup', 0, 1);

SELECT 'Flyway baseline registered at version 2.' AS status;
