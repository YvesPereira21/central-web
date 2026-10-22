ALTER TABLE articles
ADD COLUMN search_vector tsvector
GENERATED ALWAYS AS (
    setweight(to_tsvector('portuguese', coalesce(title, '')), 'A') ||
    setweight(to_tsvector('portuguese', coalesce(content, '')), 'B')
) STORED;

CREATE INDEX idx_articles_search_vector ON articles USING gin(search_vector);

ALTER TABLE questions
ADD COLUMN search_vector tsvector
GENERATED ALWAYS AS (
    setweight(to_tsvector('portuguese', coalesce(title, '')), 'A') ||
    setweight(to_tsvector('portuguese', coalesce(content, '')), 'B')
) STORED;

CREATE INDEX idx_questions_search_vector ON questions USING gin(search_vector);