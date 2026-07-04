USE influencer_db;

-- Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- Drop existing tables (order matters due to foreign keys)
DROP TABLE IF EXISTS bids;
DROP TABLE IF EXISTS campaigns;

-- Recreate tables with correct schema
CREATE TABLE campaigns (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    brand_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(100) NOT NULL,
    location VARCHAR(255) NOT NULL,
    budget DECIMAL(10, 2) NOT NULL,
    deadline DATE NOT NULL,
    status ENUM('DRAFT', 'ACTIVE', 'CLOSED', 'COMPLETED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (brand_id) REFERENCES brand_profiles(id) ON DELETE CASCADE
);

CREATE TABLE bids (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    campaign_id BIGINT NOT NULL,
    influencer_id BIGINT NOT NULL,
    proposed_budget DECIMAL(10, 2) NOT NULL,
    message TEXT,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'WITHDRAWN') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE,
    FOREIGN KEY (influencer_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_bid (campaign_id, influencer_id)
);

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;
