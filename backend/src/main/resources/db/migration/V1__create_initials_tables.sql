CREATE TABLE users(
    user_id UUID PRIMARY KEY,
    username VARCHAR(80) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(40) NOT NULL,
);

CREATE TABLE tags(
    tag_id UUID PRIMARY KEY,
    technology_name VARCHAR(80) NOT NULL UNIQUE,
    color VARCHAR(80) NOT NULL,
)

CREATE TABLE profiles(
    profile_id UUID PRIMARY KEY,
    bio TEXT NOT NULL,
    profile_type VARCHAR(30) NOT NULL,
    expertise VARCHAR(80) NOT NULL,
    level VARCHAR(80) NOT NULL,
    reputation_score BIGINT NOT NULL,
    professional BOOLEAN DEFAULT FALSE,
    user_id UUID REFERENCES users(user_id)
)

CREATE TABLE questions(
    question_id UUID PRIMARY KEY,
    title VARCHAR(40) NOT NULL,
    content TEXT NOT NULL,
    published BOOLEAN DEFAULT FALSE,
    solutioned BOOLEAN DEFAULT FALSE,
    created_at DATE NOT NULL,
    profile_id UUID REFERENCES profiles(profile_id)
)

CREATE TABLE question_tags(
    question_id UUID NOT NULL REFERENCES questions(question_id),
    tag_id UUID NOT NULL REFERENCES tags(tag_id),

    PRIMARY KEY(question_id, tag_id)
)

CREATE TABLE question_likes(
    question_id UUID NOT NULL REFERENCES questions(question_id),
    profile_id UUID NOT NULL REFERENCES profiles(profile_id),

    PRIMARY KEY(question_id, profile_id)
)

CREATE TABLE articles(
    article_id UUID PRIMARY KEY,
    title VARCHAR(40) NOT NULL,
    content TEXT NOT NULL,
    published BOOLEAN DEFAULT FALSE,
    created_at DATE NOT NULL,
    profile_id UUID REFERENCES profiles(profile_id)
)

CREATE TABLE article_tags(
    article_id UUID NOT NULL REFERENCES articles(article_id),
    tag_id UUID NOT NULL REFERENCES tags(tag_id),

    PRIMARY KEY(article_id, tag_id)
)

CREATE TABLE article_likes(
    article_id UUID NOT NULL REFERENCES articles(article_id),
    profile_id UUID NOT NULL REFERENCES profiles(profile_id),

    PRIMARY KEY(article_id, profile_id)
)

CREATE TABLE answers(
    answer_id UUID PRIMARY KEY,
    content TEXT NOT NULL,
    accepted BOOLEAN DEFAULT FALSE,
    created_at DATE NOT NULL,
    profile_id UUID REFERENCES profiles(profile_id),
    question_id UUID REFERENCES questions(question_id)
)

CREATE TABLE answer_likes(
    answer_id UUID NOT NULL REFERENCES answers(answer_id),
    profile_id UUID NOT NULL REFERENCES profiles(profile_id),

    PRIMARY KEY(answer_id, profile_id)
)

CREATE TABLE qualifications(
    qualification_id UUID PRIMARY KEY,
    job_title VARCHAR(80) NOT NULL,
    experience_leve VARCHAR(2) NOT NULL,
    institution VARCHAR(80) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    verified BOOLEAN DEFAULT FALSE,
    profile_id UUID REFERENCES profiles(profile_id)
)