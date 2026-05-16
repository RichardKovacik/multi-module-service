-- 1. Insert Users (using 'username' as the unique conflict key)
INSERT INTO users (username, first_name, last_name, password, gender, email_verified, token_version, enabled)
VALUES
    ('jdoe', 'John', 'Doe', 'password123', 'M', TRUE, 1, TRUE),
    ('jsmith', 'Jane', 'Smith', 'password123', 'F', TRUE, 1, TRUE),
    ('ajohnson', 'Alice', 'Johnson', 'password123', 'F', FALSE, 1, TRUE),
    ('bbrown', 'Bob', 'Brown', 'password123', 'M', TRUE, 1, TRUE),
    ('cdavis', 'Charlie', 'Davis', 'password123', 'M', FALSE, 1, TRUE),
    ('dmiller', 'Diana', 'Miller', 'password123', 'F', TRUE, 1, TRUE),
    ('ewilson', 'Eve', 'Wilson', 'password123', 'F', FALSE, 1, TRUE),
    ('fmoore', 'Frank', 'Moore', 'password123', 'M', TRUE, 1, TRUE),
    ('gtaylor', 'Grace', 'Taylor', 'password123', 'F', TRUE, 1, TRUE),
    ('handerson', 'Henry', 'Anderson', 'password123', 'M', FALSE, 1, TRUE)
ON CONFLICT (username) DO NOTHING;
--
-- -- 2. Insert Contacts linked by username to ensure ID alignment
INSERT INTO contact (user_id, email, phone_number)
SELECT id, username || '@example.com', '+421900' || id
FROM users
WHERE username IN ('jdoe', 'jsmith', 'ajohnson', 'bbrown', 'cdavis', 'dmiller', 'ewilson', 'fmoore', 'gtaylor', 'handerson')
ON CONFLICT (email) DO NOTHING;


-- 4. Map Roles to Users (Join Table user_role)
-- Assign 'ADMIN' to jdoe, and 'USER' to everyone else
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM users u, role r
WHERE u.username = 'jdoe' AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM users u, role r
WHERE u.username IN ('jsmith', 'ajohnson', 'bbrown', 'cdavis', 'dmiller', 'ewilson', 'fmoore', 'gtaylor', 'handerson')
  AND r.name = 'USER'
ON CONFLICT DO NOTHING;