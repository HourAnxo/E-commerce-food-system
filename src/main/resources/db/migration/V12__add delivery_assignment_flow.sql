-- Adds the "Assigned" state so a delivery can wait for a driver to answer,
-- plus a log of declines so the same driver is never re-offered the same job.

ALTER TABLE delivery
    MODIFY COLUMN delivery_status
    ENUM('Preparing','Assigned','Shipped','Delivered','Completed','Disputed')
    NOT NULL DEFAULT 'Preparing';

-- A delivery sitting in the pool has no driver yet, so these must allow NULL.
ALTER TABLE delivery
    MODIFY COLUMN delivery_person VARCHAR(255) NULL,
    MODIFY COLUMN delivery_phone  VARCHAR(255) NULL;

ALTER TABLE delivery
    ADD COLUMN assigned_at  DATETIME    NULL,
    ADD COLUMN accept_token VARCHAR(64) NULL,
    ADD UNIQUE KEY uk_delivery_accept_token (accept_token);

CREATE TABLE delivery_decline (
                                  id              INT AUTO_INCREMENT PRIMARY KEY,
                                  delivery_id     INT          NOT NULL,
                                  delivery_person VARCHAR(100) NOT NULL,
                                  declined_at     DATETIME     NOT NULL,
                                  UNIQUE KEY uk_delivery_person (delivery_id, delivery_person),
                                  CONSTRAINT fk_decline_delivery
                                      FOREIGN KEY (delivery_id) REFERENCES delivery (delivery_id)
                                          ON DELETE CASCADE
);
