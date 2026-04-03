-- liquibase formatted sql

-- changeset agasen:001-create-saga-state-table
CREATE TABLE IF NOT EXISTS saga_state (
    saga_id     UUID            NOT NULL PRIMARY KEY,
    saga_type   VARCHAR(100)    NOT NULL,
    order_id    BIGINT          NOT NULL UNIQUE REFERENCES orders(id),
    status      VARCHAR(50)     NOT NULL,
    version     BIGINT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
);

-- changeset agasen:002-create-saga-state-indexes
CREATE INDEX IF NOT EXISTS idx_saga_state_order_id ON saga_state(order_id);
CREATE INDEX IF NOT EXISTS idx_saga_state_status    ON saga_state(status);

-- changeset agasen:003-create-saga-participant-sequence
CREATE SEQUENCE IF NOT EXISTS saga_participant_seq
    START WITH 1
    INCREMENT BY 50;

-- changeset agasen:004-create-saga-participant-table
CREATE TABLE IF NOT EXISTS saga_participant (
    id          BIGINT          NOT NULL DEFAULT nextval('saga_participant_seq') PRIMARY KEY,
    saga_id     UUID            NOT NULL REFERENCES saga_state(saga_id),
    participant VARCHAR(100)    NOT NULL,
    required    BOOLEAN         NOT NULL DEFAULT FALSE,
    status      VARCHAR(50)     NOT NULL,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    CONSTRAINT uq_saga_participant UNIQUE (saga_id, participant)
);
