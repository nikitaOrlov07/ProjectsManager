CREATE TABLE IF NOT EXISTS roles (
                                     id SERIAL PRIMARY KEY,
                                     name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
                                     id SERIAL PRIMARY KEY,
                                     username VARCHAR(50) NOT NULL,
                                     email VARCHAR(100) NOT NULL,
                                     password VARCHAR(100) NOT NULL,
                                     role_id INT REFERENCES roles(id)
);

INSERT INTO roles (name) VALUES ('USER'), ('ADMIN') ON CONFLICT (name) DO NOTHING;

INSERT INTO users (username, email, password, role_id)
VALUES ('ADMIN', 'ADMIN', '$2a$10$CtnkjDK6RKEr8gu9e5KamOgy4E/7ThUgrWjw6SWxsm/8UmSDrKMoy', (SELECT id FROM roles WHERE name = 'ADMIN'))
ON CONFLICT (username) DO NOTHING;