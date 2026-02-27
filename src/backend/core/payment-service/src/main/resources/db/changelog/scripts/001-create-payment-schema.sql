-- liquibase formatted sql

-- changeset agasen:001-create-payments-sequence
CREATE SEQUENCE IF NOT EXISTS payments_seq
    START WITH 1
    INCREMENT BY 50;

-- changeset agasen:002-create-payments-table
CREATE TABLE IF NOT EXISTS payments (
    id              BIGINT          NOT NULL DEFAULT nextval('payments_seq') PRIMARY KEY,
    order_id        BIGINT          NOT NULL,
    user_id         VARCHAR(255)    NOT NULL,
    amount          DECIMAL(19, 2)  NOT NULL,
    currency        VARCHAR(10),
    status          VARCHAR(50)     NOT NULL,
    gateway_ref     VARCHAR(255),
    failure_reason  VARCHAR(500),
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP
);
