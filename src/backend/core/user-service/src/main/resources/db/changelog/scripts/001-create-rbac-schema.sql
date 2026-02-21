--liquibase formatted sql

--changeset agasen:001-create-rbac-schema

-- Sequences for GenerationType.AUTO (Hibernate default sequence strategy)
CREATE SEQUENCE IF NOT EXISTS users_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS roles_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS permission_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS user_role_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS role_permission_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS users (
    id         BIGINT PRIMARY KEY DEFAULT nextval('users_seq'),
    username   VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    email      VARCHAR(255),
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    enabled    BOOLEAN NOT NULL DEFAULT FALSE,
    locked     BOOLEAN NOT NULL DEFAULT FALSE,
    deleted    BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS roles (
    id          BIGINT PRIMARY KEY DEFAULT nextval('roles_seq'),
    name        VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS permission (
    id          BIGINT PRIMARY KEY DEFAULT nextval('permission_seq'),
    name        VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS user_role (
    id      BIGINT PRIMARY KEY DEFAULT nextval('user_role_seq'),
    user_id BIGINT NOT NULL REFERENCES users(id),
    role_id BIGINT NOT NULL REFERENCES roles(id),
    UNIQUE (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS role_permission (
    id            BIGINT PRIMARY KEY DEFAULT nextval('role_permission_seq'),
    role_id       BIGINT NOT NULL REFERENCES roles(id),
    permission_id BIGINT NOT NULL REFERENCES permission(id),
    UNIQUE (role_id, permission_id)
);
