-- Demo seed data so a freshly migrated database has something to show.
-- Category references use subqueries (not hard-coded ids) so this works whether
-- the tables start empty or already contain rows.

INSERT INTO category (category_name) VALUES
    ('Burgers'),
    ('Pizza'),
    ('Drinks'),
    ('Desserts');

INSERT INTO products (category_id, product_name, description, price, stock_quantity, image_url) VALUES
    ((SELECT category_id FROM category WHERE category_name = 'Burgers' LIMIT 1),
     'Classic Cheeseburger', 'Beef patty, cheddar, lettuce, tomato and house sauce.', 6.50, 50,
     'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=600'),
    ((SELECT category_id FROM category WHERE category_name = 'Burgers' LIMIT 1),
     'Double Bacon Burger', 'Two beef patties with crispy bacon and melted cheese.', 8.90, 40,
     'https://images.unsplash.com/photo-1553979459-d2229ba7433b?w=600'),
    ((SELECT category_id FROM category WHERE category_name = 'Pizza' LIMIT 1),
     'Margherita Pizza', 'Tomato, fresh mozzarella and basil on a thin crust.', 9.00, 30,
     'https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=600'),
    ((SELECT category_id FROM category WHERE category_name = 'Pizza' LIMIT 1),
     'Pepperoni Pizza', 'Loaded with pepperoni and a blend of cheeses.', 10.50, 30,
     'https://images.unsplash.com/photo-1628840042765-356cda07504e?w=600'),
    ((SELECT category_id FROM category WHERE category_name = 'Drinks' LIMIT 1),
     'Fresh Lemonade', 'Iced lemonade with mint.', 2.50, 100,
     'https://images.unsplash.com/photo-1621263764928-df1444c5e859?w=600'),
    ((SELECT category_id FROM category WHERE category_name = 'Desserts' LIMIT 1),
     'Chocolate Brownie', 'Warm fudge brownie with a scoop of vanilla.', 4.00, 25,
     'https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=600');

-- Demo admin account (plaintext password, matching the app's auth model).
INSERT INTO admin (full_name, email, password) VALUES
    ('Demo Admin', 'admin@foodapp.com', 'admin123');
