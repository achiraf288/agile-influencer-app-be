USE influencer_db;

-- Insert sample campaigns (assuming brand profile id exists)
INSERT INTO campaigns (brand_id, title, description, category, location, budget, deadline, status) VALUES
(1, 'Summer Fashion Campaign', 'Looking for influencers to promote our summer collection', 'Fashion', 'New York', 5000.00, '2024-12-31', 'ACTIVE'),
(1, 'Tech Product Launch', 'Influencers needed for new smartphone launch', 'Technology', 'San Francisco', 10000.00, '2024-11-30', 'ACTIVE'),
(1, 'Fitness Brand Promotion', 'Fitness influencers for gym wear promotion', 'Fitness', 'Los Angeles', 3000.00, '2024-10-15', 'DRAFT');

-- Insert sample bids (assuming user ids exist)
INSERT INTO bids (campaign_id, influencer_id, proposed_budget, message, status) VALUES
(1, 2, 4500.00, 'I have 100k followers in fashion niche with 5% engagement rate', 'PENDING'),
(1, 3, 4000.00, 'Fashion influencer with 80k followers, highly engaged audience', 'PENDING'),
(2, 2, 8000.00, 'Tech reviewer with 150k subscribers, perfect for product launch', 'ACCEPTED'),
(2, 4, 9500.00, 'Tech influencer with 200k followers, great reach', 'PENDING');
