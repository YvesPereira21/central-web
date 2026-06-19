package io.centralweb.backend.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tags")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID tagId;
    @Column(name = "technology_name", unique = true)
    private String technologyName;
    @Column(name = "color")
    private String color;
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<Question> questions = new HashSet<>();
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<Article> articles = new HashSet<>();

    public Tag() {
    }

    public Tag(UUID tagId, String technologyName, String color, Set<Question> questions, Set<Article> articles) {
        this.tagId = tagId;
        this.technologyName = technologyName;
        this.color = color;
        this.questions = questions;
        this.articles = articles;
    }

    public UUID getTagId() {
        return tagId;
    }

    public String getTechnologyName() {
        return technologyName;
    }

    public void setTechnologyName(String technologyName) {
        this.technologyName = technologyName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Set<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(Set<Question> questions) {
        this.questions = questions;
    }

    public Set<Article> getArticles() {
        return articles;
    }

    public void setArticles(Set<Article> articles) {
        this.articles = articles;
    }
}

