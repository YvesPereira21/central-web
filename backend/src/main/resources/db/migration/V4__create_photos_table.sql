CREATE TABLE photos (
    photo_id UUID PRIMARY KEY,
    photo_url VARCHAR(255) NOT NULL,
    profile_id UUID NOT NULL UNIQUE,
    CONSTRAINT fk_photos_profile FOREIGN KEY (profile_id) REFERENCES profiles(profile_id) ON DELETE CASCADE
);
