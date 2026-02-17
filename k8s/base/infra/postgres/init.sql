-- Creates additional databases beyond the default POSTGRES_DB
CREATE DATABASE user_management;
GRANT ALL PRIVILEGES ON DATABASE user_management TO product_user;
