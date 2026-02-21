--liquibase formatted sql

--changeset agasen:002-seed-products

-- Sample Categories
INSERT INTO categories (id, name, description, created_at, updated_at) VALUES
    (nextval('categories_seq'), 'Electronics', 'Electronic devices and gadgets', NOW(), NOW()),
    (nextval('categories_seq'), 'Books', 'Physical and digital books', NOW(), NOW());

-- Sample Products
INSERT INTO products (id, name, description, sku, slug, brand, price, currency, stock, created_at, updated_at) VALUES
    (nextval('products_seq'), 'Smartphone X', 'Latest model smartphone', 'SM-X-001', 'smartphone-x', 'TechCorp', 999.99, 'USD', 50, NOW(), NOW()),
    (nextval('products_seq'), 'Laptop Pro 15', 'High performance laptop', 'LP-PRO-15', 'laptop-pro-15', 'ProSystems', 1499.00, 'USD', 20, NOW(), NOW()),
    (nextval('products_seq'), 'Clean Code', 'A Handbook of Agile Software Craftsmanship', 'BOOK-CC-01', 'clean-code-book', 'Prentice Hall', 35.50, 'USD', 100, NOW(), NOW());
