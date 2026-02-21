--liquibase formatted sql

--changeset agasen:002-seed-default-data

-- Default permissions
INSERT INTO permission (id, name, description) VALUES
    (nextval('permission_seq'), 'USER_CREATE',    'Create users'),
    (nextval('permission_seq'), 'USER_READ',      'Read users'),
    (nextval('permission_seq'), 'USER_UPDATE',    'Update users'),
    (nextval('permission_seq'), 'USER_DELETE',    'Delete users'),
    (nextval('permission_seq'), 'ROLE_MANAGE',    'Manage roles'),
    (nextval('permission_seq'), 'PRODUCT_READ',   'Read products'),
    (nextval('permission_seq'), 'PRODUCT_CREATE', 'Create products'),
    (nextval('permission_seq'), 'PRODUCT_UPDATE', 'Update products'),
    (nextval('permission_seq'), 'PRODUCT_DELETE', 'Delete products');

-- Default roles
INSERT INTO roles (id, name, description) VALUES
    (nextval('roles_seq'), 'ADMIN', 'Administrator with full access'),
    (nextval('roles_seq'), 'USER',  'Regular user with read-only access');

-- Assign all permissions to ADMIN role
INSERT INTO role_permission (id, role_id, permission_id)
SELECT nextval('role_permission_seq'), r.id, p.id
FROM roles r, permission p
WHERE r.name = 'ADMIN';

-- Assign read permissions to USER role
INSERT INTO role_permission (id, role_id, permission_id)
SELECT nextval('role_permission_seq'), r.id, p.id
FROM roles r, permission p
WHERE r.name = 'USER'
  AND p.name IN ('USER_READ', 'PRODUCT_READ');

-- Default admin user (password: 'pass' BCrypt-encoded)
INSERT INTO users (id, username, password, email, first_name, last_name, enabled, locked, deleted)
VALUES (nextval('users_seq'), 'admin', '$2b$12$uiNDNdAUzR9HlFy1qobxrOVp6ut0lFaPWD4Lke/zey3mJ592.PApu', 'admin@shopbuddy.com', 'Admin', 'User', true, false, false);

-- Assign ADMIN role to admin user
INSERT INTO user_role (id, user_id, role_id)
SELECT nextval('user_role_seq'), u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ADMIN';
