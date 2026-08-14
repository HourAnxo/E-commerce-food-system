-- Links a customer account to a Telegram chat, so the bot knows who is talking
-- to it and can push delivery notifications back.
--
-- Nullable: most customers never use the bot. Unique so one Telegram chat maps
-- to at most one account (MySQL allows many NULLs under a UNIQUE index).
ALTER TABLE customer
    ADD COLUMN telegram_chat_id BIGINT NULL,
    ADD CONSTRAINT uq_customer_telegram_chat_id UNIQUE (telegram_chat_id);
