ALTER TABLE delivery MODIFY COLUMN delivery_status
    ENUM('Preparing','Shipped','Delivered','Completed','Disputed') DEFAULT 'Preparing';