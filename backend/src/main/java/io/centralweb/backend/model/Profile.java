package io.centralweb.backend.model;

import io.centralweb.backend.enums.ProfileType;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Formula;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "profile_id")
    private UUID profileId;
    @Column(name = "name")
    private String name;
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;
    private ProfileType profileType;
    @Column(name = "level")
    private String level = "Novato";
    @Column(name = "reputation_score")
    private long reputationScore = 0;
    @Column(name = "professional")
    private boolean professional = false;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;
    @OneToOne(mappedBy = "profile", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Photo photo;
    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Question> questions = new ArrayList<>();
    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Article> articles = new ArrayList<>();
    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Answer> answers = new ArrayList<>();
    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Qualification> qualifications = new ArrayList<>();
    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Collection> collections = new ArrayList<>();
    @Formula("(SELECT COUNT(*) FROM articles a WHERE a.profile_id = profile_id)")
    private Long articlesCreatedByProfile;
    @Formula("(SELECT COUNT(*) FROM answers a WHERE a.profile_id = profile_id AND a.accepted = true)")
    private Long answersAccepted;

    public Profile() {
    }

    public Profile(UUID profileId, String bio, String name, ProfileType profileType, String level, long reputationScore, boolean professional, User user, Photo photo, List<Question> questions, List<Article> articles, List<Answer> answers, List<Qualification> qualifications, List<Collection> collections, Long articlesCreatedByProfile, Long answersAccepted) {
        this.profileId = profileId;
        this.bio = bio;
        this.name = name;
        this.profileType = profileType;
        this.level = level;
        this.reputationScore = reputationScore;
        this.professional = professional;
        this.user = user;
        this.photo = photo;
        this.questions = questions;
        this.articles = articles;
        this.answers = answers;
        this.qualifications = qualifications;
        this.collections = collections;
        this.articlesCreatedByProfile = articlesCreatedByProfile;
        this.answersAccepted = answersAccepted;
    }

    public UUID getProfileId() {
        return profileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public ProfileType getProfileType() {
        return profileType;
    }

    public void setProfileType(ProfileType profileType) {
        this.profileType = profileType;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public long getReputationScore() {
        return reputationScore;
    }

    public void setReputationScore(long reputationScore) {
        this.reputationScore = reputationScore;
    }

    public boolean isProfessional() {
        return professional;
    }

    public void setProfessional(boolean professional) {
        this.professional = professional;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Photo getPhoto() {
        return photo;
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    public List<Qualification> getQualifications() {
        return qualifications;
    }

    public void setQualifications(List<Qualification> qualifications) {
        this.qualifications = qualifications;
    }

    public List<Collection> getCollections() {
        return collections;
    }

    public void setCollections(List<Collection> collections) {
        this.collections = collections;
    }

    public Long getArticlesCreatedByProfile() {
        return articlesCreatedByProfile == null ? 0 : articlesCreatedByProfile;
    }

    public Long getAnswersAccepted() {
        return answersAccepted == null ? 0 : answersAccepted;
    }
}

