CREATE TABLE refresh_tokens (
    refresh_token_id UUID NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    user_id UUID UNIQUE,
    PRIMARY KEY (refresh_token_id),
    CONSTRAINT fk_refresh_tokens_user_id FOREIGN KEY (user_id) REFERENCES users (user_id)
);