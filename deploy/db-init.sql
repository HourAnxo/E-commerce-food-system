-- One-time database setup. Flyway (run by the app on startup) creates all
-- tables from src/main/resources/db/migration, so this only needs to create
-- an empty schema and the application's MySQL user.
--
-- Run as the MySQL root/admin user:  sudo mysql < db-init.sql
-- Change the password to match DB_PASSWORD in foodapp.service before running.

CREATE DATABASE IF NOT EXISTS e_commerce_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'foodapp'@'localhost' IDENTIFIED BY 'CHANGE_ME_STRONG_PW';
GRANT ALL PRIVILEGES ON e_commerce_system.* TO 'foodapp'@'localhost';
FLUSH PRIVILEGES;
