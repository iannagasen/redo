-- liquibase formatted sql

-- changeset agasen:004-create-idempotency-records-sequence
CREATE SEQUENCE IF NOT EXISTS idempotency_records_seq
    START WITH 1
    INCREMENT BY 50;

-- changeset agasen:005-create-idempotency-records-table
CREATE TABLE IF NOT EXISTS idempotency_records (
    id               BIGINT       NOT NULL DEFAULT nextval('idempotency_records_seq') PRIMARY KEY,
    idempotency_key  UUID         NOT NULL,
    user_id          VARCHAR(255) NOT NULL,
    response_body    TEXT         NOT NULL,
    created_at       TIMESTAMP,
    updated_at       TIMESTAMP
);

-- changeset agasen:006-add-unique-idempotency-key-user
ALTER TABLE idempotency_records
ADD CONSTRAINT uk_idempotency_key_user UNIQUE (idempotency_key, user_id);
