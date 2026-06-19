package io.centralweb.backend.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.UUID;

@Entity
@Table(name = "photos")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID photoId;
    @Column(name = "photo_url")
    private String photo_url;
    @OneToOne
    @JoinColumn(name = "profile_id")
    private Profile profile;

    public Photo() {}

    public Photo(String photo_url, Profile profile) {
        this.photo_url = photo_url;
        this.profile = profile;
    }

    public UUID getPhotoId() {
        return photoId;
    }

    public void setPhotoId(UUID photoId) {
        this.photoId = photoId;
    }

    public String getPhoto_url() {
        return photo_url;
    }

    public void setPhoto_url(String photo_url) {
        this.photo_url = photo_url;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
