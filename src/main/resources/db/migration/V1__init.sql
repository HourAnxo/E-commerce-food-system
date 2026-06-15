-- Base schema for the E-Commerce Food System.
-- Tables are created in foreign-key dependency order. Later migrations add the
-- product discount column (V2) and rename the delivery status enum value (V3),
-- so this file intentionally omits products.discount_percentage and uses the
-- original delivery_status enum.

CREATE TABLE admin (
    admin_id   INT NOT NULL AUTO_INCREMENT,
    full_name  VARCHAR(100) NOT NULL,
    email      VARCHAR(100) NOT NULL,
    password   VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE category (
    category_id   INT NOT NULL AUTO_INCREMENT,
    category_name VARCHAR(100) NOT NULL,
    PRIMARY KEY (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE customer (
    customer_id INT NOT NULL AUTO_INCREMENT,
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL,
    phone       VARCHAR(20) NOT NULL,
    address     TEXT NOT NULL,
    created_at  TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (customer_id),
    UNIQUE KEY email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE products (
    product_id     INT NOT NULL AUTO_INCREMENT,
    category_id    INT DEFAULT NULL,
    product_name   VARCHAR(100) NOT NULL,
    description    TEXT,
    price          DECIMAL(10,2) NOT NULL,
    stock_quantity INT DEFAULT 0,
    image_url      VARCHAR(255) DEFAULT NULL,
    created_at     TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (product_id),
    KEY products_ibfk_1 (category_id),
    CONSTRAINT products_ibfk_1 FOREIGN KEY (category_id) REFERENCES category (category_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE cart (
    cart_id     INT NOT NULL AUTO_INCREMENT,
    customer_id INT NOT NULL,
    created_at  TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (cart_id),
    KEY customer_id (customer_id),
    CONSTRAINT cart_ibfk_1 FOREIGN KEY (customer_id) REFERENCES customer (customer_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE cart_item (
    cart_item_id INT NOT NULL AUTO_INCREMENT,
    cart_id      INT NOT NULL,
    product_id   INT NOT NULL,
    quantity     INT NOT NULL,
    PRIMARY KEY (cart_item_id),
    KEY cart_item_ibfk_1 (cart_id),
    KEY cart_item_ibfk_2 (product_id),
    CONSTRAINT cart_item_ibfk_1 FOREIGN KEY (cart_id) REFERENCES cart (cart_id) ON DELETE CASCADE,
    CONSTRAINT cart_item_ibfk_2 FOREIGN KEY (product_id) REFERENCES products (product_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE orders (
    order_id     INT NOT NULL AUTO_INCREMENT,
    customer_id  INT NOT NULL,
    order_date   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL,
    order_status ENUM('Pending','Processing','Shipped','Delivery','Cancelled') DEFAULT 'Pending',
    PRIMARY KEY (order_id),
    KEY customer_id (customer_id),
    CONSTRAINT orders_ibfk_1 FOREIGN KEY (customer_id) REFERENCES customer (customer_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE delivery (
    delivery_id        INT NOT NULL AUTO_INCREMENT,
    order_id           INT NOT NULL,
    delivery_person    VARCHAR(100) NOT NULL,
    delivery_phone     VARCHAR(20) NOT NULL,
    delivery_address   TEXT NOT NULL,
    delivery_status    ENUM('Preparing','On the way','Delivered') DEFAULT 'Preparing',
    estimated_delivery DATETIME DEFAULT NULL,
    PRIMARY KEY (delivery_id),
    KEY order_id (order_id),
    CONSTRAINT delivery_ibfk_1 FOREIGN KEY (order_id) REFERENCES orders (order_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE payment (
    payment_id     INT NOT NULL AUTO_INCREMENT,
    order_id       INT NOT NULL,
    payment_method ENUM('Cash','Credit Card','ABA','ACELEDA','Wing') NOT NULL,
    payment_status ENUM('Pending','Paid','Failed') DEFAULT 'Pending',
    payment_date   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (payment_id),
    KEY order_id (order_id),
    CONSTRAINT payment_ibfk_1 FOREIGN KEY (order_id) REFERENCES orders (order_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE review (
    review_id   INT NOT NULL AUTO_INCREMENT,
    customer_id INT NOT NULL,
    product_id  INT NOT NULL,
    rating      INT DEFAULT NULL,
    comment     TEXT,
    created_at  TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (review_id),
    KEY review_ibfk_1 (customer_id),
    KEY review_ibfk_2 (product_id),
    CONSTRAINT review_ibfk_1 FOREIGN KEY (customer_id) REFERENCES customer (customer_id) ON DELETE CASCADE,
    CONSTRAINT review_ibfk_2 FOREIGN KEY (product_id) REFERENCES products (product_id) ON DELETE CASCADE,
    CONSTRAINT review_chk_1 CHECK ((rating BETWEEN 1 AND 5))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
