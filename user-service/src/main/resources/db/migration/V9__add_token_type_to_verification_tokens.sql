ALTER TABLE verification_token
    ADD verification_token_type VARCHAR(50);

ALTER TABLE verification_token
    ALTER COLUMN verification_token_type SET NOT NULL;