-- 1. Insert Users (using 'username' as the unique conflict key)
INSERT INTO users (username, first_name, last_name, password, gender, token_version, status, created_at, updated_at)
SELECT username, first_name, last_name, password, gender, 1, status, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (VALUES
    ('jdoe', 'John', 'Doe', 'password123', 'M', 'ACTIVE'),
    ('jsmith', 'Jane', 'Smith', 'password123', 'F', 'ACTIVE'),
    ('ajohnson', 'Alice', 'Johnson', 'password123', 'F', 'PENDING_VERIFICATION'),
    ('bbrown', 'Bob', 'Brown', 'password123', 'M', 'ACTIVE'),
    ('cdavis', 'Charlie', 'Davis', 'password123', 'M', 'PENDING_VERIFICATION'),
    ('dmiller', 'Diana', 'Miller', 'password123', 'F', 'ACTIVE'),
    ('ewilson', 'Eve', 'Wilson', 'password123', 'F', 'PENDING_VERIFICATION'),
    ('fmoore', 'Frank', 'Moore', 'password123', 'M', 'ACTIVE'),
    ('gtaylor', 'Grace', 'Taylor', 'password123', 'F', 'ACTIVE'),
    ('handerson', 'Henry', 'Anderson', 'password123', 'M', 'PENDING_VERIFICATION'),
    ('imartin', 'Irene', 'Martin', 'password123', 'F', 'ACTIVE'),
    ('jthomas', 'Jack', 'Thomas', 'password123', 'M', 'ACTIVE'),
    ('kjackson', 'Karen', 'Jackson', 'password123', 'F', 'PENDING_VERIFICATION'),
    ('lwhite', 'Liam', 'White', 'password123', 'M', 'ACTIVE'),
    ('mharris', 'Mia', 'Harris', 'password123', 'F', 'ACTIVE'),
    ('nclark', 'Noah', 'Clark', 'password123', 'M', 'PENDING_VERIFICATION'),
    ('nlewis', 'Nora', 'Lewis', 'password123', 'F', 'ACTIVE'),
    ('owalker', 'Owen', 'Walker', 'password123', 'M', 'ACTIVE'),
    ('phall', 'Paige', 'Hall', 'password123', 'F', 'ACTIVE'),
    ('pyoung', 'Peter', 'Young', 'password123', 'M', 'PENDING_VERIFICATION'),
    ('rallen', 'Rachel', 'Allen', 'password123', 'F', 'ACTIVE'),
    ('sking', 'Samuel', 'King', 'password123', 'M', 'ACTIVE'),
    ('swright', 'Sophia', 'Wright', 'password123', 'F', 'PENDING_VERIFICATION'),
    ('tlopez', 'Thomas', 'Lopez', 'password123', 'M', 'ACTIVE'),
    ('vscott', 'Victoria', 'Scott', 'password123', 'F', 'ACTIVE')
) AS dev_users (username, first_name, last_name, password, gender, status)
ON CONFLICT (username) DO NOTHING;

-- 2. Insert Contacts linked by username to ensure ID alignment
INSERT INTO contact (user_id, email, phone_number)
SELECT id, username || '@example.com', '+421900' || id
FROM users
WHERE username IN (
    'jdoe', 'jsmith', 'ajohnson', 'bbrown', 'cdavis', 'dmiller', 'ewilson', 'fmoore', 'gtaylor', 'handerson',
    'imartin', 'jthomas', 'kjackson', 'lwhite', 'mharris', 'nclark', 'nlewis', 'owalker', 'phall', 'pyoung',
    'rallen', 'sking', 'swright', 'tlopez', 'vscott'
)
ON CONFLICT DO NOTHING;


-- 3. Map Roles to Users (Join Table user_role)
-- Assign 'ADMIN' to jdoe, and 'USER' to everyone else
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM users u, role r
WHERE u.username = 'jdoe' AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM users u, role r
WHERE u.username IN (
    'jsmith', 'ajohnson', 'bbrown', 'cdavis', 'dmiller', 'ewilson', 'fmoore', 'gtaylor', 'handerson',
    'imartin', 'jthomas', 'kjackson', 'lwhite', 'mharris', 'nclark', 'nlewis', 'owalker', 'phall', 'pyoung',
    'rallen', 'sking', 'swright', 'tlopez', 'vscott'
)
  AND r.name = 'USER'
ON CONFLICT DO NOTHING;