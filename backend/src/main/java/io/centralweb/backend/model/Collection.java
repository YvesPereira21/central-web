package io.centralweb.backend.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "collections")
@EqualsAndHashCode
@ToString
public class Collection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "collection_id")
    private UUID collectionId;
    @Column(name = "name", nullable = false)
    private String name;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;
    @ManyToMany
    @JoinTable(
            name = "collection_articles",
            joinColumns = @JoinColumn(name = "collection_id"),
            inverseJoinColumns = @JoinColumn(name = "article_id")
    )
    private List<Article> articles = new ArrayList<>();
    @ManyToMany
    @JoinTable(
            name = "collection_questions",
            joinColumns = @JoinColumn(name = "collection_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private List<Question> questions = new ArrayList<>();

    public Collection() {
    }

    public Collection(UUID collectionId, String name, Profile profile, List<Article> articles, List<Question> questions) {
        this.collectionId = collectionId;
        this.name = name;
        this.profile = profile;
        this.articles = articles;
        this.questions = questions;
    }

    public UUID getCollectionId() {
        return collectionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public void addArticle(Article article) {
        if (!this.articles.contains(article)) {
            this.articles.add(article);
        }
    }

    public void removeArticle(Article article) {
        this.articles.remove(article);
    }

    public void addQuestion(Question question) {
        if (!this.questions.contains(question)) {
            this.questions.add(question);
        }
    }

    public void removeQuestion(Question question) {
        this.questions.remove(question);
    }
}
