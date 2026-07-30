CREATE DATABASE IF NOT EXISTS influencer_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'influencer_user'@'localhost' IDENTIFIED BY 'influencer_password';
GRANT ALL PRIVILEGES ON influencer_db.* TO 'influencer_user'@'localhost';
FLUSH PRIVILEGES;

USE influencer_db;

-- Tables are created by Hibernate because spring.jpa.hibernate.ddl-auto=update.
-- Add INSERT statements here if you want starter data.