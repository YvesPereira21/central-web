CREATE TABLE users (
    user_id UUID PRIMARY KEY,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(255),
    role VARCHAR(40) NOT NULL,
    provider VARCHAR(40) DEFAULT 'LOCAL',
    provider_id VARCHAR(255)
);

CREATE TABLE tags (
    tag_id UUID PRIMARY KEY,
    technology_name VARCHAR(80) NOT NULL UNIQUE,
    color VARCHAR(80) NOT NULL
);

CREATE TABLE profiles (
    profile_id UUID PRIMARY KEY,
    name VARCHAR(80) NOT NULL,
    bio TEXT NOT NULL,
    profile_type VARCHAR(30) NOT NULL,
    level VARCHAR(80) NOT NULL,
    reputation_score BIGINT NOT NULL,
    professional BOOLEAN DEFAULT FALSE,
    user_id UUID REFERENCES users(user_id)
);

CREATE TABLE photos (
    photo_id UUID PRIMARY KEY,
    photo_url VARCHAR(255) NOT NULL,
    profile_id UUID NOT NULL UNIQUE,
    CONSTRAINT fk_photos_profile FOREIGN KEY (profile_id) REFERENCES profiles(profile_id) ON DELETE CASCADE
);

CREATE TABLE questions (
    question_id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    published BOOLEAN DEFAULT FALSE,
    solutioned BOOLEAN DEFAULT FALSE,
    created_at DATE NOT NULL,
    profile_id UUID REFERENCES profiles(profile_id)
);

CREATE TABLE question_tags (
    question_id UUID NOT NULL REFERENCES questions(question_id),
    tag_id UUID NOT NULL REFERENCES tags(tag_id),

    PRIMARY KEY(question_id, tag_id)
);

CREATE TABLE question_likes (
    question_id UUID NOT NULL REFERENCES questions(question_id),
    profile_id UUID NOT NULL REFERENCES profiles(profile_id),

    PRIMARY KEY(question_id, profile_id)
);

CREATE TABLE articles (
    article_id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    published BOOLEAN DEFAULT FALSE,
    created_at DATE NOT NULL,
    profile_id UUID REFERENCES profiles(profile_id)
);

CREATE TABLE article_tags (
    article_id UUID NOT NULL REFERENCES articles(article_id),
    tag_id UUID NOT NULL REFERENCES tags(tag_id),

    PRIMARY KEY(article_id, tag_id)
);

CREATE TABLE article_likes (
    article_id UUID NOT NULL REFERENCES articles(article_id),
    profile_id UUID NOT NULL REFERENCES profiles(profile_id),

    PRIMARY KEY(article_id, profile_id)
);

CREATE TABLE answers (
    answer_id UUID PRIMARY KEY,
    content TEXT NOT NULL,
    accepted BOOLEAN DEFAULT FALSE,
    created_at DATE NOT NULL,
    profile_id UUID REFERENCES profiles(profile_id),
    question_id UUID REFERENCES questions(question_id)
);

CREATE TABLE answer_likes (
    answer_id UUID NOT NULL REFERENCES answers(answer_id),
    profile_id UUID NOT NULL REFERENCES profiles(profile_id),

    PRIMARY KEY(answer_id, profile_id)
);

CREATE TABLE qualifications (
    qualification_id UUID PRIMARY KEY,
    job_title VARCHAR(80) NOT NULL,
    experience_level VARCHAR(2) NOT NULL,
    institution VARCHAR(80) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    verified BOOLEAN DEFAULT FALSE,
    profile_id UUID REFERENCES profiles(profile_id)
);

CREATE TABLE collections (
    collection_id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    profile_id UUID NOT NULL REFERENCES profiles(profile_id)
);

CREATE TABLE collection_articles (
    collection_id UUID NOT NULL REFERENCES collections(collection_id),
    article_id UUID NOT NULL REFERENCES articles(article_id),
    
    PRIMARY KEY(collection_id, article_id)
);

CREATE TABLE collection_questions (
    collection_id UUID NOT NULL REFERENCES collections(collection_id),
    question_id UUID NOT NULL REFERENCES questions(question_id),
    
    PRIMARY KEY(collection_id, question_id)
);

CREATE TABLE comments (
    comment_id UUID PRIMARY KEY,
    content TEXT NOT NULL,
    created_at DATE NOT NULL,
    profile_id UUID REFERENCES profiles(profile_id),
    answer_id UUID REFERENCES answers(answer_id)
);

CREATE TABLE refresh_tokens (
    refresh_token_id UUID NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    user_id UUID UNIQUE,
    PRIMARY KEY (refresh_token_id),
    CONSTRAINT fk_refresh_tokens_user_id FOREIGN KEY (user_id) REFERENCES users (user_id)
);