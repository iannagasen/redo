--liquibase formatted sql

--changeset agasen:001-create-product-schema

CREATE SEQUENCE IF NOT EXISTS products_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS categories_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS categories (
    id          BIGINT PRIMARY KEY DEFAULT nextval('categories_seq'),
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    parent_id   BIGINT REFERENCES categories(id),
    created_at  TIMESTAMP WITH TIME ZONE,
    updated_at  TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS products (
    id          BIGINT PRIMARY KEY DEFAULT nextval('products_seq'),
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    sku         VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) NOT NULL,
    brand       VARCHAR(255),
    price       DECIMAL(19, 2),
    currency    VARCHAR(10),
    stock       INTEGER NOT NULL DEFAULT 0,
    bought      INTEGER NOT NULL DEFAULT 0,
    cart        INTEGER NOT NULL DEFAULT 0,
    created_at  TIMESTAMP WITH TIME ZONE,
    updated_at  TIMESTAMP WITH TIME ZONE
);
