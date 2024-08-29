CREATE DATABASE test;

CREATE USER 'test'@'%' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON test.* TO 'test'@'%';
