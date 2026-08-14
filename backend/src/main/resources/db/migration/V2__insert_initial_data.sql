INSERT INTO users (user_id, email, password, role, provider, provider_id) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'admin@centralweb.io', '$2a$10$pKS2xiDvG68m.johqeYO9.Cljo5hKH3J6BiudX3owW7geuywOVe5q', 'ADMIN', 'LOCAL', NULL),
('c1111111-1111-1111-1111-111111111111', 'lucas.silva@centralweb.io', '$2a$10$pKS2xiDvG68m.johqeYO9.Cljo5hKH3J6BiudX3owW7geuywOVe5q', 'PERSON', 'LOCAL', NULL),
('d1111111-1111-1111-1111-111111111111', 'mariana.costa@centralweb.io', '$2a$10$pKS2xiDvG68m.johqeYO9.Cljo5hKH3J6BiudX3owW7geuywOVe5q', 'PERSON', 'LOCAL', NULL),
('e1111111-1111-1111-1111-111111111111', 'carlos.eduardo@centralweb.io', '$2a$10$pKS2xiDvG68m.johqeYO9.Cljo5hKH3J6BiudX3owW7geuywOVe5q', 'PERSON', 'LOCAL', NULL),
('f1111111-1111-1111-1111-111111111111', 'ana.beatris@centralweb.io', '$2a$10$pKS2xiDvG68m.johqeYO9.Cljo5hKH3J6BiudX3owW7geuywOVe5q', 'PERSON', 'LOCAL', NULL);

INSERT INTO profiles (profile_id, name, bio, profile_type, level, reputation_score, professional, user_id) VALUES
('b1ffcd00-0d0c-5fa9-cc7e-7cc0ce491b22', 'Administrador', 'Perfil Administrativo do Sistema Central Web', 'SELFTAUGHT', 'Compilador Humano', 5000, TRUE, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'),
('c2222222-2222-2222-2222-222222222222', 'Lucas Silva', 'Desenvolvedor Backend apaixonado por Java, Spring Boot e arquitetura de microsserviços.', 'GRADUATE', 'Mestre do Código', 1250, TRUE, 'c1111111-1111-1111-1111-111111111111'),
('d2222222-2222-2222-2222-222222222222', 'Mariana Costa', 'Frontend Developer focada em Angular, UX/UI e acessibilidade na web.', 'UNDERGRADUATE', 'Arquiteta da Interface', 890, TRUE, 'd1111111-1111-1111-1111-111111111111'),
('e2222222-2222-2222-2222-222222222222', 'Carlos Eduardo', 'Entusiasta de DevOps, Docker, Kubernetes e CI/CD.', 'SELFTAUGHT', 'Novato', 340, FALSE, 'e1111111-1111-1111-1111-111111111111'),
('f2222222-2222-2222-2222-222222222222', 'Ana Beatris', 'Desenvolvedora Fullstack aprendendo novas tecnologias a cada dia.', 'UNDERGRADUATE', 'Novato', 150, FALSE, 'f1111111-1111-1111-1111-111111111111');

INSERT INTO tags (tag_id, technology_name, color) VALUES
('t1111111-0000-0000-0000-000000000001', 'Java', '#f89820'),
('t1111111-0000-0000-0000-000000000002', 'Spring Boot', '#6db33f'),
('t1111111-0000-0000-0000-000000000003', 'Angular', '#dd0031'),
('t1111111-0000-0000-0000-000000000004', 'TypeScript', '#3178c6'),
('t1111111-0000-0000-0000-000000000005', 'PostgreSQL', '#336791'),
('t1111111-0000-0000-0000-000000000006', 'Docker', '#2496ed');

INSERT INTO qualifications (qualification_id, job_title, experience_level, institution, start_date, end_date, verified, profile_id) VALUES
('q1111111-1111-1111-1111-111111111111', 'Desenvolvedor Backend Sr', 'SR', 'Google', '2021-01-15', '2024-02-28', TRUE, 'c2222222-2222-2222-2222-222222222222'),
('q1111111-1111-1111-1111-111111111112', 'Engenheiro de Software', 'SR', 'Mercado Livre', '2024-03-01', NULL, TRUE, 'c2222222-2222-2222-2222-222222222222'),
('q2222222-2222-2222-2222-222222222221', 'Desenvolvedora Frontend Pl', 'PL', 'Nubank', '2022-06-01', NULL, TRUE, 'd2222222-2222-2222-2222-222222222222'),
('q2222222-2222-2222-2222-222222222222', 'Estagiária de UI/UX', 'JR', 'Globo', '2020-02-01', '2022-05-30', TRUE, 'd2222222-2222-2222-2222-222222222222'),
('q3333333-3333-3333-3333-333333333331', 'DevOps Engineer', 'PL', 'AWS', '2023-01-10', NULL, FALSE, 'e2222222-2222-2222-2222-222222222222'),
('q4444444-4444-4444-4444-444444444441', 'Desenvolvedora Fullstack', 'JR', 'Tech Solutions', '2023-08-01', NULL, TRUE, 'f2222222-2222-2222-2222-222222222222');

INSERT INTO articles (article_id, title, content, published, created_at, profile_id) VALUES
('a1111111-1111-1111-1111-111111111111', 'Como Otimizar Consultas JPA com Spring Data', 'O uso do Spring Data JPA facilita imensamente o desenvolvimento, mas pode causar problemas graves de performance como N+1 queries se não utilizado corretamente. Neste artigo abordamos JOIN FETCH, EntityGraph e DTO Projections para consultas ultra rápidas.', TRUE, '2026-08-01', 'c2222222-2222-2222-2222-222222222222'),
('a2222222-2222-2222-2222-222222222222', 'Guia Definitivo de Angular Signals no v18+', 'Com o advento dos Signals, o Angular mudou drasticamente a forma de gerenciar reatividade e estado. Entenda como eliminar o uso desnecessário de RxJS em componentes e otimizar o Change Detection com a estratégia OnPush.', TRUE, '2026-08-05', 'd2222222-2222-2222-2222-222222222222'),
('a3333333-3333-3333-3333-333333333333', 'Criando Pipelines CI/CD com Docker e GitHub Actions', 'Automatizar a compilação, testes e deploy de uma aplicação fullstack é indispensável em ambientes modernos. Veja o passo a passo para construir contêineres otimizados e publicar em produção.', TRUE, '2026-08-10', 'e2222222-2222-2222-2222-222222222222');

INSERT INTO article_tags (article_id, tag_id) VALUES
('a1111111-1111-1111-1111-111111111111', 't1111111-0000-0000-0000-000000000001'),
('a1111111-1111-1111-1111-111111111111', 't1111111-0000-0000-0000-000000000002'),
('a1111111-1111-1111-1111-111111111111', 't1111111-0000-0000-0000-000000000005'),
('a2222222-2222-2222-2222-222222222222', 't1111111-0000-0000-0000-000000000003'),
('a2222222-2222-2222-2222-222222222222', 't1111111-0000-0000-0000-000000000004'),
('a3333333-3333-3333-3333-333333333333', 't1111111-0000-0000-0000-000000000006');

INSERT INTO article_likes (article_id, profile_id) VALUES
('a1111111-1111-1111-1111-111111111111', 'd2222222-2222-2222-2222-222222222222'),
('a1111111-1111-1111-1111-111111111111', 'e2222222-2222-2222-2222-222222222222'),
('a1111111-1111-1111-1111-111111111111', 'f2222222-2222-2222-2222-222222222222'),
('a2222222-2222-2222-2222-222222222222', 'c2222222-2222-2222-2222-222222222222'),
('a2222222-2222-2222-2222-222222222222', 'f2222222-2222-2222-2222-222222222222');

INSERT INTO questions (question_id, title, content, published, solutioned, created_at, profile_id) VALUES
('q1111111-0000-0000-0000-111111111111', 'Como resolver LazyInitializationException no JPA?', 'Estou recebendo o erro org.hibernate.LazyInitializationException: could not initialize proxy - no Session ao tentar acessar uma lista de filhes fora da transação. Qual a melhor maneira de resolver sem carregar tudo com EAGER?', TRUE, TRUE, '2026-08-03', 'f2222222-2222-2222-2222-222222222222'),
('q2222222-0000-0000-0000-222222222222', 'Qual a forma correta de usar @Input com Signals no Angular 18?', 'Gostaria de saber como definir inputs reativos no Angular novo utilizando a função input() ao invés do decorator antigo @Input(). Alguém teria um exemplo prático?', TRUE, FALSE, '2026-08-08', 'e2222222-2222-2222-2222-222222222222');

INSERT INTO question_tags (question_id, tag_id) VALUES
('q1111111-0000-0000-0000-111111111111', 't1111111-0000-0000-0000-000000000001'),
('q1111111-0000-0000-0000-111111111111', 't1111111-0000-0000-0000-000000000002'),
('q2222222-0000-0000-0000-222222222222', 't1111111-0000-0000-0000-000000000003'),
('q2222222-0000-0000-0000-222222222222', 't1111111-0000-0000-0000-000000000004');

INSERT INTO answers (answer_id, content, accepted, created_at, profile_id, question_id) VALUES
('ans11111-1111-1111-1111-111111111111', 'Esse erro acontece porque a sessão do Hibernate já foi fechada quando você tenta acessar a coleção LAZY. Você pode resolver utilizando `JOIN FETCH` na sua consulta JPQL, por exemplo: `SELECT p FROM Profile p JOIN FETCH p.qualifications WHERE p.id = :id`. Outra alternativa limpa é utilizar DTOs com projections.', TRUE, '2026-08-03', 'c2222222-2222-2222-2222-222222222222', 'q1111111-0000-0000-0000-111111111111'),
('ans22222-2222-2222-2222-222222222222', 'No Angular 18 você pode declarar: `myProp = input<string>();` ou `myRequiredProp = input.required<string>();`. Para reagir a mudanças, use o `effect()` ou `computed()`.', FALSE, '2026-08-09', 'd2222222-2222-2222-2222-222222222222', 'q2222222-0000-0000-0000-222222222222');

INSERT INTO comments (comment_id, content, created_at, profile_id, answer_id) VALUES
('com11111-1111-1111-1111-111111111111', 'Excelente explicação sobre JOIN FETCH! Resolveu exatamente o meu problema.', '2026-08-04', 'f2222222-2222-2222-2222-222222222222', 'ans11111-1111-1111-1111-111111111111');

INSERT INTO collections (collection_id, name, profile_id) VALUES
('col11111-1111-1111-1111-111111111111', 'Boas Práticas Spring & Java', 'c2222222-2222-2222-2222-222222222222'),
('col22222-2222-2222-2222-222222222222', 'Estudos de Frontend Moderno', 'd2222222-2222-2222-2222-222222222222');

INSERT INTO collection_articles (collection_id, article_id) VALUES
('col11111-1111-1111-1111-111111111111', 'a1111111-1111-1111-1111-111111111111'),
('col22222-2222-2222-2222-222222222222', 'a2222222-2222-2222-2222-222222222222');
