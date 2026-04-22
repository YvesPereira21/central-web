package io.centralweb.backend.model;

import io.centralweb.backend.enums.ProfileType;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@EqualsAndHashCode
@ToString
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "profile_id")
    private UUID profileId;
    @Column(name = "name")
    private String name;
    @Column(name = "bio")
    private String bio;
    private ProfileType profileType;
    @Column(name = "expertise")
    private String expertise;
    @Column(name = "level")
    private String level;
    @Column(name = "reputation_score")
    private long reputationScore;
    @Column(name = "professional")
    private boolean professional;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    @OneToMany(mappedBy = "profile")
    private List<Question> questions;
    @OneToMany(mappedBy = "profile")
    private List<Article> articles;
    @OneToMany(mappedBy = "profile")
    private List<Answer> answers;
    @OneToMany(mappedBy = "profile")
    private List<Qualification> qualifications;

    public Profile() {
    }

    public Profile(UUID profileId, String name, String bio, ProfileType profileType, String expertise, String level, long reputationScore, boolean professional, User user, List<Question> questions, List<Article> articles, List<Answer> answers, List<Qualification> qualifications) {
        this.profileId = profileId;
        this.name = name;
        this.bio = bio;
        this.profileType = profileType;
        this.expertise = expertise;
        this.level = level;
        this.reputationScore = reputationScore;
        this.professional = professional;
        this.user = user;
        this.questions = questions;
        this.articles = articles;
        this.answers = answers;
        this.qualifications = qualifications;
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

    public String getExpertise() {
        return expertise;
    }

    public void setExpertise(String expertise) {
        this.expertise = expertise;
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
}

