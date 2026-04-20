package io.centralweb.backend.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tags")
@EqualsAndHashCode
@ToString
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "tag_id")
    private UUID tagId;
    @Column(name = "technology_name", unique = true)
    private String technologyName;
    @Column(name = "color")
    private String color;
    @ManyToMany(mappedBy = "tags")
    private List<Question> questions;
    @ManyToMany(mappedBy = "tags")
    private List<Article> articles;

    public Tag() {
    }

    public Tag(UUID tagId, String technologyName, String color, List<Question> questions, List<Article> articles) {
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
}

