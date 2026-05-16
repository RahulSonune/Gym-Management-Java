-- Hibernate maps String columns to VARCHAR; align legacy CHAR columns.
ALTER TABLE organization MODIFY currency_code VARCHAR(3) NOT NULL DEFAULT 'INR';
ALTER TABLE branch MODIFY country VARCHAR(2) NULL;
ALTER TABLE membership_plan MODIFY currency_code VARCHAR(3) NOT NULL DEFAULT 'INR';
ALTER TABLE invoice MODIFY currency_code VARCHAR(3) NOT NULL DEFAULT 'INR';
ALTER TABLE payment MODIFY currency_code VARCHAR(3) NOT NULL DEFAULT 'INR';
