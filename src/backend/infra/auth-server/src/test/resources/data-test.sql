-- Test admin user (password: 'pass' BCrypt-encoded)
INSERT INTO users (username, password, email, first_name, last_name, enabled, locked, deleted)
VALUES ('admin', '$2b$12$uiNDNdAUzR9HlFy1qobxrOVp6ut0lFaPWD4Lke/zey3mJ592.PApu', 'admin@test.com', 'Admin', 'User', true, false, false);

INSERT INTO roles (name, description) VALUES ('ADMIN', 'Administrator');
INSERT INTO permission (name, description) VALUES ('PRODUCT_READ', 'Read products');
INSERT INTO permission (name, description) VALUES ('PRODUCT_CREATE', 'Create products');

INSERT INTO user_role (user_id, role_id) VALUES (1, 1);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 1);
INSERT INTO role_permission (role_id, permission_id) VALUES (1, 2);
