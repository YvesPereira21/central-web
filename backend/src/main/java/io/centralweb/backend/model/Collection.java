package io.centralweb.backend.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "collections")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Collection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID collectionId;
    @Column(name = "name", nullable = false)
    private String name;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "collection_articles",
            joinColumns = @JoinColumn(name = "collection_id"),
            inverseJoinColumns = @JoinColumn(name = "article_id")
    )
    private Set<Article> articles = new HashSet<>();
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "collection_questions",
            joinColumns = @JoinColumn(name = "collection_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private Set<Question> questions = new HashSet<>();

    public Collection() {
    }

    public Collection(UUID collectionId, String name, Profile profile, Set<Article> articles, Set<Question> questions) {
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

    public Set<Article> getArticles() {
        return articles;
    }

    public void setArticles(Set<Article> articles) {
        this.articles = articles;
    }

    public Set<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(Set<Question> questions) {
        this.questions = questions;
    }

    public void addArticle(Article article) {
        this.articles.add(article);
    }

    public void removeArticle(Article article) {
        this.articles.remove(article);
    }

    public void addQuestion(Question question) {
        this.questions.add(question);
    }

    public void removeQuestion(Question question) {
        this.questions.remove(question);
    }
}
