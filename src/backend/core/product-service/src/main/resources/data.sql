DELETE
FROM products;
DELETE
FROM categories;

-- Insert sample categoriesx
INSERT INTO categories (name, description, slug)
VALUES ('Electronics', 'Electronic devices and gadgets', 'electronics'),
       ('Clothing', 'Fashion and apparel', 'clothing'),
       ('Books', 'Books and literature', 'books'),
       ('Home & Garden', 'Home improvement and garden supplies', 'home-garden');

-- Insert sample products
INSERT INTO products (name, description, sku, slug, brand, price, stock_quantity, category_id, attributes)
VALUES ('iPhone 15 Pro', 'Latest iPhone with advanced features', 'IPHONE-15-PRO-128', 'iphone-15-pro', 'Apple', 999.99,
        50,
        (SELECT id FROM categories WHERE slug = 'electronics'),
        '{"color": "Space Black", "storage": "128GB", "screen_size": "6.1 inches"}'),
       ('Cotton T-Shirt', 'Comfortable cotton t-shirt', 'COTTON-TSHIRT-M-BLUE', 'cotton-tshirt-blue', 'Generic', 19.99,
        100,
        (SELECT id FROM categories WHERE slug = 'clothing'),
        '{"size": "M", "color": "Blue", "material": "100% Cotton"}'),
       ('The Great Gatsby', 'Classic American novel', 'BOOK-GATSBY-PB', 'great-gatsby', 'Scribner', 12.99, 25,
        (SELECT id FROM categories WHERE slug = 'books'),
        '{"author": "F. Scott Fitzgerald", "pages": 180, "format": "Paperback"}');