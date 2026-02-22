-- liquibase formatted sql

-- changeset agasen:001-create-orders-sequence
CREATE SEQUENCE IF NOT EXISTS orders_seq
    START WITH 1
    INCREMENT BY 50;

-- changeset agasen:002-create-order-items-sequence
CREATE SEQUENCE IF NOT EXISTS order_items_seq
    START WITH 1
    INCREMENT BY 50;

-- changeset agasen:003-create-orders-table
CREATE TABLE IF NOT EXISTS orders (
    id          BIGINT          NOT NULL DEFAULT nextval('orders_seq') PRIMARY KEY,
    user_id     VARCHAR(255)    NOT NULL,
    status      VARCHAR(50)     NOT NULL,
    total       DECIMAL(19, 2)  NOT NULL,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
);

-- changeset agasen:004-create-order-items-table
CREATE TABLE IF NOT EXISTS order_items (
    id           BIGINT          NOT NULL DEFAULT nextval('order_items_seq') PRIMARY KEY,
    order_id     BIGINT          NOT NULL REFERENCES orders(id),
    product_id   BIGINT          NOT NULL,
    product_name VARCHAR(255)    NOT NULL,
    brand        VARCHAR(255),
    price        DECIMAL(19, 2)  NOT NULL,
    currency     VARCHAR(10),
    quantity     INT             NOT NULL,
    line_total   DECIMAL(19, 2)  NOT NULL,
    created_at   TIMESTAMP,
    updated_at   TIMESTAMP
);
