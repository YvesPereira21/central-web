package io.centralweb.backend.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Formula;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "answers")
@EqualsAndHashCode
@ToString
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "answer_id")
    private UUID answerId;
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    @Column(name = "accepted")
    private boolean accepted;
    @Column(name = "created_at")
    private LocalDate createdAt;
    @ManyToOne
    @JoinColumn(name = "profile_id")
    private Profile profile;
    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;
    @ManyToMany
    @JoinTable(
            name = "answer_likes",
            joinColumns = @JoinColumn(name = "answer_id"),
            inverseJoinColumns = @JoinColumn(name = "profile_id")
    )
    private List<Profile> answerLikes;
    @Formula("(SELECT COUNT(*) FROM answer_likes al WHERE al.answer_id = answer_id)")
    private Long answerTotalLikes;

    public Answer() {
    }

    public Answer(UUID answerId, String content, boolean accepted, LocalDate createdAt, Profile profile, Question question, List<Profile> answerLikes) {
        this.answerId = answerId;
        this.content = content;
        this.accepted = accepted;
        this.createdAt = createdAt;
        this.profile = profile;
        this.question = question;
        this.answerLikes = answerLikes;
    }

    public UUID getAnswerId() {
        return answerId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
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

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public List<Profile> getAnswerLikes() {
        return answerLikes;
    }

    public void setAnswerLikes(List<Profile> answerLikes) {
        this.answerLikes = answerLikes;
    }

    public Long getAnswerTotalLikes(){
        return answerTotalLikes == null ? 0 : answerTotalLikes;
    }
}

