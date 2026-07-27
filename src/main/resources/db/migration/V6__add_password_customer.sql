-- src/main/resources/db/migration/V{next}__add_password_to_customer.sql
ALTER TABLE customer ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT '';