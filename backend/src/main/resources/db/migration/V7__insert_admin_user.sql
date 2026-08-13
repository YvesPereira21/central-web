-- Inserção do usuário ADMIN padrão no banco de dados via Flyway
-- Senha: admin123 (encriptada com BCrypt)
INSERT INTO users (user_id, email, password, role, provider, provider_id)
VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'admin@centralweb.io',
    '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.85UXy.0pYn/hG3V.dG6eq',
    'ADMIN',
    'LOCAL',
    NULL
)
ON CONFLICT (email) DO NOTHING;
