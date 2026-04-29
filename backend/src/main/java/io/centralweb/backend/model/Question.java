package io.centralweb.backend.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Formula;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "questions")
@EqualsAndHashCode
@ToString
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "question_id")
    private UUID questionId;
    @Column(name = "title")
    private String title;
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    @Column(name = "published")
    private boolean published = false;
    @Column(name = "solutioned")
    private boolean solutioned = false;
    @Column(name = "created_at")
    private LocalDate createdAt;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profile_id")
    private Profile profile;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "question_tags",
            joinColumns = @JoinColumn(name = "question_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;
    @OneToMany(mappedBy = "question", fetch =  FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Answer> answers;
    @ManyToMany
    @JoinTable(
            name = "question_likes",
            joinColumns = @JoinColumn(name = "question_id"),
            inverseJoinColumns = @JoinColumn(name = "profile_id")
    )
    private List<Profile> questionLikes;
    @Formula("(SELECT COUNT(*) FROM question_likes ql WHERE ql.question_id = question_id)")
    private Long questionTotalLikes;

    public Question() {
    }

    public Question(UUID questionId, String title, String content, boolean published, boolean solutioned, LocalDate createdAt, Profile profile, List<Tag> tags, List<Answer> answers, List<Profile> questionLikes) {
        this.questionId = questionId;
        this.title = title;
        this.content = content;
        this.published = published;
        this.solutioned = solutioned;
        this.createdAt = createdAt;
        this.profile = profile;
        this.tags = tags;
        this.answers = answers;
        this.questionLikes = questionLikes;
    }

    public UUID getQuestionId() {
        return questionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public boolean isSolutioned() {
        return solutioned;
    }

    public void setSolutioned(boolean solutioned) {
        this.solutioned = solutioned;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    public List<Profile> getQuestionLikes() {
        return questionLikes;
    }

    public void setQuestionLikes(List<Profile> questionLikes) {
        this.questionLikes = questionLikes;
    }

    public void addLike(Profile profile){
        this.questionLikes.add(profile);
    }

    public void removeLike(Profile profile){
        this.questionLikes.add(profile);
    }

    public Long getQuestionTotalLikes() {
        return questionTotalLikes == null ? 0 : questionTotalLikes;
    }
}

