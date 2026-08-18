-- -----------------------------------------------------
-- DADOS INICIAIS: USERS
-- -----------------------------------------------------
INSERT INTO users (user_id, email, password, role, provider, provider_id) VALUES
('a0000000-0000-0000-0000-000000000001', 'admin@centralweb.io', '$2a$10$pKS2xiDvG68m.johqeYO9.Cljo5hKH3J6BiudX3owW7geuywOVe5q', 'ADMIN', 'LOCAL', NULL),
('a0000000-0000-0000-0000-000000000002', 'lucas.silva@centralweb.io', '$2a$10$pKS2xiDvG68m.johqeYO9.Cljo5hKH3J6BiudX3owW7geuywOVe5q', 'PERSON', 'LOCAL', NULL),
('a0000000-0000-0000-0000-000000000003', 'mariana.costa@centralweb.io', '$2a$10$pKS2xiDvG68m.johqeYO9.Cljo5hKH3J6BiudX3owW7geuywOVe5q', 'PERSON', 'LOCAL', NULL),
('a0000000-0000-0000-0000-000000000004', 'carlos.eduardo@centralweb.io', '$2a$10$pKS2xiDvG68m.johqeYO9.Cljo5hKH3J6BiudX3owW7geuywOVe5q', 'PERSON', 'LOCAL', NULL),
('a0000000-0000-0000-0000-000000000005', 'ana.beatris@centralweb.io', '$2a$10$pKS2xiDvG68m.johqeYO9.Cljo5hKH3J6BiudX3owW7geuywOVe5q', 'PERSON', 'LOCAL', NULL),
('a0000000-0000-0000-0000-000000000006', 'bruno.oliveira@centralweb.io', '$2a$10$pKS2xiDvG68m.johqeYO9.Cljo5hKH3J6BiudX3owW7geuywOVe5q', 'PERSON', 'LOCAL', NULL),
('a0000000-0000-0000-0000-000000000007', 'fernanda.lima@centralweb.io', '$2a$10$pKS2xiDvG68m.johqeYO9.Cljo5hKH3J6BiudX3owW7geuywOVe5q', 'PERSON', 'LOCAL', NULL),
('a0000000-0000-0000-0000-000000000008', 'gabriel.santos@centralweb.io', '$2a$10$pKS2xiDvG68m.johqeYO9.Cljo5hKH3J6BiudX3owW7geuywOVe5q', 'PERSON', 'LOCAL', NULL),
('a0000000-0000-0000-0000-000000000009', 'patricia.alves@centralweb.io', '$2a$10$pKS2xiDvG68m.johqeYO9.Cljo5hKH3J6BiudX3owW7geuywOVe5q', 'PERSON', 'LOCAL', NULL),
('a0000000-0000-0000-0000-000000000010', 'rodrigo.mendes@centralweb.io', '$2a$10$pKS2xiDvG68m.johqeYO9.Cljo5hKH3J6BiudX3owW7geuywOVe5q', 'PERSON', 'LOCAL', NULL);

-- -----------------------------------------------------
-- DADOS INICIAIS: PROFILES
-- -----------------------------------------------------
INSERT INTO profiles (profile_id, name, bio, profile_type, level, reputation_score, professional, user_id) VALUES
('b0000000-0000-0000-0000-000000000001', 'Administrador', 'Perfil Administrativo do Sistema Central Web', 'autodidata', 'Compilador Humano', 5000, TRUE, 'a0000000-0000-0000-0000-000000000001'),
('b0000000-0000-0000-0000-000000000002', 'Lucas Silva', 'Desenvolvedor Backend apaixonado por Java, Spring Boot e arquitetura de microsserviços.', 'profissional', 'Mestre do Código', 1250, TRUE, 'a0000000-0000-0000-0000-000000000002'),
('b0000000-0000-0000-0000-000000000003', 'Mariana Costa', 'Frontend Developer focada em Angular, UX/UI e acessibilidade na web.', 'universitario', 'Arquiteta da Interface', 890, TRUE, 'a0000000-0000-0000-0000-000000000003'),
('b0000000-0000-0000-0000-000000000004', 'Carlos Eduardo', 'Entusiasta de DevOps, Docker, Kubernetes e CI/CD.', 'autodidata', 'Veterano', 640, FALSE, 'a0000000-0000-0000-0000-000000000004'),
('b0000000-0000-0000-0000-000000000005', 'Ana Beatris', 'Desenvolvedora Fullstack aprendendo novas tecnologias a cada dia.', 'universitario', 'Praticante', 250, FALSE, 'a0000000-0000-0000-0000-000000000005'),
('b0000000-0000-0000-0000-000000000006', 'Bruno Oliveira', 'Arquiteto de Software especialista em Java, Kotlin e alta disponibilidade.', 'profissional', 'Domador de Legado', 2850, TRUE, 'a0000000-0000-0000-0000-000000000006'),
('b0000000-0000-0000-0000-000000000007', 'Fernanda Lima', 'Engenheira Frontend especialista em sistemas de design e animações.', 'profissional', 'Visionário', 1100, TRUE, 'a0000000-0000-0000-0000-000000000007'),
('b0000000-0000-0000-0000-000000000008', 'Gabriel Santos', 'Especialista em Segurança da Informação, OWASP e DevSecOps.', 'autodidata', 'Veterano', 780, TRUE, 'a0000000-0000-0000-0000-000000000008'),
('b0000000-0000-0000-0000-000000000009', 'Patricia Alves', 'Engenheira de Dados apaixonada por bancos de dados relacionais e Redis.', 'profissional', 'Visionário', 1420, TRUE, 'a0000000-0000-0000-0000-000000000009'),
('b0000000-0000-0000-0000-000000000010', 'Rodrigo Mendes', 'Engenheiro de Qualidade focado em automação de testes e performance.', 'universitario', 'Praticante', 410, FALSE, 'a0000000-0000-0000-0000-000000000010');

-- -----------------------------------------------------
-- DADOS INICIAIS: TAGS
-- -----------------------------------------------------
INSERT INTO tags (tag_id, technology_name, color) VALUES
('c0000000-0000-0000-0000-000000000001', 'Java', '#f89820'),
('c0000000-0000-0000-0000-000000000002', 'Spring Boot', '#6db33f'),
('c0000000-0000-0000-0000-000000000003', 'Angular', '#dd0031'),
('c0000000-0000-0000-0000-000000000004', 'TypeScript', '#3178c6'),
('c0000000-0000-0000-0000-000000000005', 'PostgreSQL', '#336791'),
('c0000000-0000-0000-0000-000000000006', 'Docker', '#2496ed'),
('c0000000-0000-0000-0000-000000000007', 'Redis', '#dc382d'),
('c0000000-0000-0000-0000-000000000008', 'Kubernetes', '#326ce5'),
('c0000000-0000-0000-0000-000000000009', 'TailwindCSS', '#06b6d4'),
('c0000000-0000-0000-0000-000000000010', 'Security', '#e11d48');

-- -----------------------------------------------------
-- DADOS INICIAIS: QUALIFICATIONS
-- -----------------------------------------------------
INSERT INTO qualifications (qualification_id, job_title, experience_level, institution, start_date, end_date, verified, profile_id) VALUES
('d0000000-0000-0000-0000-000000000001', 'Desenvolvedor Backend Sr', 'SR', 'Google', '2021-01-15', '2024-02-28', TRUE, 'b0000000-0000-0000-0000-000000000002'),
('d0000000-0000-0000-0000-000000000002', 'Engenheiro de Software', 'SR', 'Mercado Livre', '2024-03-01', NULL, TRUE, 'b0000000-0000-0000-0000-000000000002'),
('d0000000-0000-0000-0000-000000000003', 'Desenvolvedora Frontend Pl', 'PL', 'Nubank', '2022-06-01', NULL, TRUE, 'b0000000-0000-0000-0000-000000000003'),
('d0000000-0000-0000-0000-000000000004', 'Estagiária de UI/UX', 'JR', 'Globo', '2020-02-01', '2022-05-30', TRUE, 'b0000000-0000-0000-0000-000000000003'),
('d0000000-0000-0000-0000-000000000005', 'DevOps Engineer', 'PL', 'AWS', '2023-01-10', NULL, FALSE, 'b0000000-0000-0000-0000-000000000004'),
('d0000000-0000-0000-0000-000000000006', 'Desenvolvedora Fullstack', 'JR', 'Tech Solutions', '2023-08-01', NULL, TRUE, 'b0000000-0000-0000-0000-000000000005'),
('d0000000-0000-0000-0000-000000000007', 'Arquiteto de Soluções Sr', 'SR', 'IBM', '2019-04-01', '2023-12-31', TRUE, 'b0000000-0000-0000-0000-000000000006'),
('d0000000-0000-0000-0000-000000000008', 'Staff Software Engineer', 'SR', 'Red Hat', '2024-01-01', NULL, TRUE, 'b0000000-0000-0000-0000-000000000006'),
('d0000000-0000-0000-0000-000000000009', 'Engenheira Frontend Sr', 'SR', 'Vercel', '2023-02-15', NULL, TRUE, 'b0000000-0000-0000-0000-000000000007'),
('d0000000-0000-0000-0000-000000000010', 'AppSec Specialist', 'PL', 'Check Point', '2022-09-01', NULL, TRUE, 'b0000000-0000-0000-0000-000000000008'),
('d0000000-0000-0000-0000-000000000011', 'Engenheira de Dados Sr', 'SR', 'Databricks', '2021-07-01', NULL, TRUE, 'b0000000-0000-0000-0000-000000000009'),
('d0000000-0000-0000-0000-000000000012', 'QA Automation Engineer', 'PL', 'Thoughtworks', '2023-05-01', NULL, FALSE, 'b0000000-0000-0000-0000-000000000010');

-- -----------------------------------------------------
-- DADOS INICIAIS: ARTICLES
-- -----------------------------------------------------
INSERT INTO articles (article_id, title, content, published, created_at, profile_id) VALUES
('e0000000-0000-0000-0000-000000000001', 'Como Otimizar Consultas JPA com Spring Data', 'O uso do Spring Data JPA facilita imensamente o desenvolvimento, mas pode causar problemas graves de performance como N+1 queries se não utilizado corretamente. Neste artigo abordamos JOIN FETCH, EntityGraph e DTO Projections para consultas ultra rápidas.', TRUE, '2026-08-01', 'b0000000-0000-0000-0000-000000000002'),
('e0000000-0000-0000-0000-000000000002', 'Guia Definitivo de Angular Signals no v19+', 'Com o advento dos Signals, o Angular mudou drasticamente a forma de gerenciar reatividade e estado. Entenda como eliminar o uso desnecessário de RxJS em componentes e otimizar o Change Detection com a estratégia OnPush.', TRUE, '2026-08-05', 'b0000000-0000-0000-0000-000000000003'),
('e0000000-0000-0000-0000-000000000003', 'Criando Pipelines CI/CD com Docker e GitHub Actions', 'Automatizar a compilação, testes e deploy de uma aplicação fullstack é indispensável em ambientes modernos. Veja o passo a passo para construir contêineres otimizados e publicar em produção.', TRUE, '2026-08-10', 'b0000000-0000-0000-0000-000000000004'),
('e0000000-0000-0000-0000-000000000004', 'Mitigando IDOR e Protegendo APIs REST com Spring Security', 'Falhas de Insecure Direct Object Reference (IDOR) figuram entre os riscos mais críticos da OWASP. Neste artigo prático, mostramos como validar ownership na camada de serviço e implementar controles com Spring Security.', TRUE, '2026-08-11', 'b0000000-0000-0000-0000-000000000008'),
('e0000000-0000-0000-0000-000000000005', 'Estratégias Avançadas de Caching com Redis e Spring Boot', 'Aprenda a utilizar Redis como camada de cache de alta performance no Spring Boot com as anotações @Cacheable e @CacheEvict, reduzindo latência e carga no PostgreSQL.', TRUE, '2026-08-12', 'b0000000-0000-0000-0000-000000000009'),
('e0000000-0000-0000-0000-000000000006', 'Design Systems com TailwindCSS e Componentes Reutilizáveis', 'Descubra como estruturar um Design System escalável no frontend Angular utilizando utilitários do TailwindCSS e bibliotecas de UI modernas.', TRUE, '2026-08-13', 'b0000000-0000-0000-0000-000000000007'),
('e0000000-0000-0000-0000-000000000007', 'Padrões de Resiliência em Microsserviços com Java 21', 'Explore padrões como Circuit Breaker, Rate Limiter e Bulkhead utilizando Java 21 e Spring Boot para criar sistemas tolerantes a falhas.', TRUE, '2026-08-14', 'b0000000-0000-0000-0000-000000000006');

-- -----------------------------------------------------
-- DADOS INICIAIS: ARTICLE_TAGS
-- -----------------------------------------------------
INSERT INTO article_tags (article_id, tag_id) VALUES
('e0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001'),
('e0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000002'),
('e0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000005'),
('e0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000003'),
('e0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000004'),
('e0000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000006'),
('e0000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000008'),
('e0000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000002'),
('e0000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000010'),
('e0000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000002'),
('e0000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000007'),
('e0000000-0000-0000-0000-000000000006', 'c0000000-0000-0000-0000-000000000003'),
('e0000000-0000-0000-0000-000000000006', 'c0000000-0000-0000-0000-000000000009'),
('e0000000-0000-0000-0000-000000000007', 'c0000000-0000-0000-0000-000000000001'),
('e0000000-0000-0000-0000-000000000007', 'c0000000-0000-0000-0000-000000000002');

-- -----------------------------------------------------
-- DADOS INICIAIS: ARTICLE_LIKES
-- -----------------------------------------------------
INSERT INTO article_likes (article_id, profile_id) VALUES
('e0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000003'),
('e0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000004'),
('e0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000005'),
('e0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000006'),
('e0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000002'),
('e0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000005'),
('e0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000007'),
('e0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000002'),
('e0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000008'),
('e0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000002'),
('e0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000006'),
('e0000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000002'),
('e0000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000006');

-- -----------------------------------------------------
-- DADOS INICIAIS: QUESTIONS
-- -----------------------------------------------------
INSERT INTO questions (question_id, title, content, published, solutioned, created_at, profile_id) VALUES
('f0000000-0000-0000-0000-000000000001', 'Como resolver LazyInitializationException no JPA?', 'Estou recebendo o erro org.hibernate.LazyInitializationException ao tentar acessar uma lista de filhes fora da transação. Qual a melhor maneira de resolver sem carregar tudo com EAGER?', TRUE, TRUE, '2026-08-03', 'b0000000-0000-0000-0000-000000000005'),
('f0000000-0000-0000-0000-000000000002', 'Qual a forma correta de usar @Input com Signals no Angular 18+?', 'Gostaria de saber como definir inputs reativos no Angular novo utilizando a função input() ao invés do decorator antigo @Input(). Alguém teria um exemplo prático?', TRUE, TRUE, '2026-08-08', 'b0000000-0000-0000-0000-000000000004'),
('f0000000-0000-0000-0000-000000000003', 'Como configurar invalidação de cache no Redis com Spring Boot?', 'Estou utilizando Spring Data Redis com a anotação @CacheEvict, mas gostaria de saber como limpar chaves específicas dinamicamente após atualizações.', TRUE, TRUE, '2026-08-09', 'b0000000-0000-0000-0000-000000000002'),
('f0000000-0000-0000-0000-000000000004', 'Como proteger Refresh Tokens com Cookies SameSite e HttpOnly?', 'Estou implementando JWT no Spring Security e quero enviar o refresh token como Cookie HttpOnly ao invés de no corpo JSON. Qual a maneira recomendada com CookieUtil?', TRUE, TRUE, '2026-08-10', 'b0000000-0000-0000-0000-000000000003'),
('f0000000-0000-0000-0000-000000000005', 'Qual a diferença entre DTO Projections e Entity Graph no JPA?', 'Em consultas muito pesadas no Spring Data JPA, quando devo preferir DTO Projections ao invés de utilizar Entity Graphs?', TRUE, FALSE, '2026-08-14', 'b0000000-0000-0000-0000-000000000010');

-- -----------------------------------------------------
-- DADOS INICIAIS: QUESTION_TAGS
-- -----------------------------------------------------
INSERT INTO question_tags (question_id, tag_id) VALUES
('f0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001'),
('f0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000002'),
('f0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000003'),
('f0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000004'),
('f0000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000002'),
('f0000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000007'),
('f0000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000002'),
('f0000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000010'),
('f0000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000001'),
('f0000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000005');

-- -----------------------------------------------------
-- DADOS INICIAIS: ANSWERS
-- -----------------------------------------------------
INSERT INTO answers (answer_id, content, accepted, created_at, profile_id, question_id) VALUES
('11000000-0000-0000-0000-000000000001', 'Esse erro acontece porque a sessão do Hibernate já foi fechada quando você tenta acessar a coleção LAZY. Você pode resolver utilizando `JOIN FETCH` na sua consulta JPQL, por exemplo: `SELECT p FROM Profile p JOIN FETCH p.qualifications WHERE p.id = :id`. Outra alternativa limpa é utilizar DTOs com projections.', TRUE, '2026-08-03', 'b0000000-0000-0000-0000-000000000002', 'f0000000-0000-0000-0000-000000000001'),
('11000000-0000-0000-0000-000000000002', 'No Angular 18+ você pode declarar: `myProp = input<string>();` ou `myRequiredProp = input.required<string>();`. Para reagir a mudanças no template ou código, use `computed()` ou `effect()`.', TRUE, '2026-08-09', 'b0000000-0000-0000-0000-000000000003', 'f0000000-0000-0000-0000-000000000002'),
('11000000-0000-0000-0000-000000000003', 'Você pode utilizar `@CacheEvict(value = "questions", key = "#id")` no método de atualização, ou `@CacheEvict(value = "questions", allEntries = true)` para limpar todo o cache da entidade.', TRUE, '2026-08-09', 'b0000000-0000-0000-0000-000000000009', 'f0000000-0000-0000-0000-000000000003'),
('11000000-0000-0000-0000-000000000004', 'Você pode utilizar `ResponseCookie.from(name, token).httpOnly(true).secure(false).path("/").sameSite("Lax").build()` no Spring MVC e definir no header `Set-Cookie`.', TRUE, '2026-08-10', 'b0000000-0000-0000-0000-000000000008', 'f0000000-0000-0000-0000-000000000004'),
('11000000-0000-0000-0000-000000000005', 'DTO Projections são recomendadas quando você só precisa de leitura rápida de dados específicos sem carregar instâncias completas de entidade na memória.', FALSE, '2026-08-14', 'b0000000-0000-0000-0000-000000000006', 'f0000000-0000-0000-0000-000000000005');

-- -----------------------------------------------------
-- DADOS INICIAIS: COMMENTS
-- -----------------------------------------------------
INSERT INTO comments (comment_id, content, created_at, profile_id, answer_id) VALUES
('22000000-0000-0000-0000-000000000001', 'Excelente explicação sobre JOIN FETCH! Resolveu exatamente o meu problema.', '2026-08-04', 'b0000000-0000-0000-0000-000000000005', '11000000-0000-0000-0000-000000000001'),
('22000000-0000-0000-0000-000000000002', 'Muito claro! O uso de input() simplificou bastante os meus componentes.', '2026-08-09', 'b0000000-0000-0000-0000-000000000004', '11000000-0000-0000-0000-000000000002'),
('22000000-0000-0000-0000-000000000003', 'Ótima dica sobre @CacheEvict!', '2026-08-10', 'b0000000-0000-0000-0000-000000000002', '11000000-0000-0000-0000-000000000003');

-- -----------------------------------------------------
-- DADOS INICIAIS: COLLECTIONS
-- -----------------------------------------------------
INSERT INTO collections (collection_id, name, profile_id) VALUES
('33000000-0000-0000-0000-000000000001', 'Boas Práticas Spring & Java', 'b0000000-0000-0000-0000-000000000002'),
('33000000-0000-0000-0000-000000000002', 'Estudos de Frontend Moderno', 'b0000000-0000-0000-0000-000000000003'),
('33000000-0000-0000-0000-000000000003', 'DevOps & Infraestrutura Cloud', 'b0000000-0000-0000-0000-000000000004');

-- -----------------------------------------------------
-- DADOS INICIAIS: COLLECTION_ARTICLES & COLLECTION_QUESTIONS
-- -----------------------------------------------------
INSERT INTO collection_articles (collection_id, article_id) VALUES
('33000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001'),
('33000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000005'),
('33000000-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000002'),
('33000000-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000006'),
('33000000-0000-0000-0000-000000000003', 'e0000000-0000-0000-0000-000000000003');

INSERT INTO collection_questions (collection_id, question_id) VALUES
('33000000-0000-0000-0000-000000000001', 'f0000000-0000-0000-0000-000000000001'),
('33000000-0000-0000-0000-000000000002', 'f0000000-0000-0000-0000-000000000002');
