CREATE TABLE collections(
    collection_id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    profile_id UUID NOT NULL REFERENCES profiles(profile_id)
);

CREATE TABLE collection_articles(
    collection_id UUID NOT NULL REFERENCES collections(collection_id),
    article_id UUID NOT NULL REFERENCES articles(article_id),
    
    PRIMARY KEY(collection_id, article_id)
);

CREATE TABLE collection_questions(
    collection_id UUID NOT NULL REFERENCES collections(collection_id),
    question_id UUID NOT NULL REFERENCES questions(question_id),
    
    PRIMARY KEY(collection_id, question_id)
);

CREATE TABLE comments(
    comment_id UUID PRIMARY KEY,
    content TEXT NOT NULL,
    created_at DATE NOT NULL,
    profile_id UUID REFERENCES profiles(profile_id),
    answer_id UUID REFERENCES answers(answer_id)
);
