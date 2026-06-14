


-- delivery_status is a native MySQL ENUM. Renaming the "On the way" value to
-- "Shipped" requires three steps: widen the ENUM to allow both the old and new
-- value, migrate existing rows, then narrow it to the final set.
--
-- This also resolves a pre-existing mismatch: the DB held 'On the way' (spaces)
-- while JPA @Enumerated(EnumType.STRING) wrote the constant name 'On_the_way'.
-- The final values (Preparing/Shipped/Delivered) are single words that match
-- the Java enum exactly.

ALTER TABLE delivery
    MODIFY delivery_status ENUM('Preparing', 'On the way', 'Shipped', 'Delivered')
    DEFAULT 'Preparing';

UPDATE delivery
SET delivery_status = 'Shipped'
WHERE delivery_status = 'On the way';

ALTER TABLE delivery
    MODIFY delivery_status ENUM('Preparing', 'Shipped', 'Delivered')
    DEFAULT 'Preparing';
