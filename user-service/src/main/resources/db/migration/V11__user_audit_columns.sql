ALTER TABLE users
    ADD created_at TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE users
    ADD status VARCHAR(30);

ALTER TABLE users
    ADD updated_at TIMESTAMP WITHOUT TIME ZONE;

UPDATE users
SET created_at = CURRENT_TIMESTAMP
WHERE created_at IS NULL;
ALTER TABLE users
    ALTER COLUMN created_at SET NOT NULL;

UPDATE users
SET status = CASE
                 WHEN enabled = TRUE AND email_verified = TRUE
                     THEN 'ACTIVE'
                 WHEN enabled = TRUE AND email_verified = FALSE
                     THEN 'PENDING_VERIFICATION'
                 WHEN enabled = FALSE
                     THEN 'DISABLED'
                 ELSE 'UNKNOWN'
    END;

UPDATE users
SET updated_at = CURRENT_TIMESTAMP
WHERE updated_at IS NULL;
ALTER TABLE users
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE users
    DROP COLUMN email_verified;

ALTER TABLE users
    DROP COLUMN enabled;