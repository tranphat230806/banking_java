
INSERT INTO admins (
    username,
    password,
    full_name,
    email,
    avatar,
    role,
    status,
    created_at,
    face_registered,
    face_image_path
) VALUES (
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Administrator',
    'admin@banking.com',
    NULL,
    'ROLE_ADMIN',
    'ACTIVE',
    NOW(),
    false,
    NULL
);
