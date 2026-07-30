-- Line items for an order. Until now an order stored only its total amount, so

-- the backend never knew which products were bought and could not deduct stock.
-- unit_price is the price at purchase time — product prices change, and an old
-- order must not silently re-price itself.

CREATE TABLE order_item (
    order_item_id INT NOT NULL AUTO_INCREMENT,
    order_id      INT NOT NULL,
    product_id    INT NOT NULL,
    quantity      INT NOT NULL,
    unit_price    DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (order_item_id),
    KEY order_id (order_id),
    KEY product_id (product_id),
    CONSTRAINT order_item_ibfk_1 FOREIGN KEY (order_id) REFERENCES orders (order_id) ON DELETE CASCADE,
    CONSTRAINT order_item_ibfk_2 FOREIGN KEY (product_id) REFERENCES products (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
