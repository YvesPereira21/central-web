-- Inserção do perfil correspondente ao usuário ADMIN
-- Busca o user_id dinamicamente pelo e-mail 'admin@centralweb.io' para evitar inconsistência de chave estrangeira
INSERT INTO profiles (profile_id, name, bio, profile_type, level, reputation_score, professional, user_id)
SELECT 
    'b1ffcd00-0d0c-5fa9-cc7e-7cc0ce491b22',
    'Administrador',
    'Perfil Administrativo do Sistema Central Web',
    'SELFTAUGHT',
    'Compilador Humano',
    5000,
    TRUE,
    u.user_id
FROM users u
WHERE u.email = 'admin@centralweb.io'
ON CONFLICT (profile_id) DO NOTHING;