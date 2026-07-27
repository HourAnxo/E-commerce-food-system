
-- The Payment entity maps amount, transaction_ref and updated_at, but the
-- payment table (created in V1) never had them. Add them so ddl-auto=validate
-- passes on a fresh (Docker/prod) database built purely from migrations.
ALTER TABLE payment
    ADD COLUMN amount          DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    ADD COLUMN transaction_ref VARCHAR(100)  NULL,
    ADD COLUMN updated_at      TIMESTAMP     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
