
    -- Add Bakong (KHQR) as an accepted payment method.
ALTER TABLE payment
    MODIFY payment_method ENUM('Cash','Credit Card','ABA','ACELEDA','Wing','Bakong') NOT NULL;
