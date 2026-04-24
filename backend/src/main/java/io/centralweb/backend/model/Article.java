package io.centralweb.backend.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Formula;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "articles")
@EqualsAndHashCode
@ToString
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "article_id")
    private UUID articleId;
    @Column(name = "title")
    private String title;
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    @Column(name = "published")
    private boolean published = false;
    @Column(name = "created_at")
    private LocalDate createdAt;
    @ManyToOne
    @JoinColumn(name = "profile_id")
    private Profile profile;
    @ManyToMany
    @JoinTable(
            name = "article_tags",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;
    @ManyToMany
    @JoinTable(
            name = "article_likes",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "profile_id")
    )
    private List<Profile> articleLikes;
    @Formula("(SELECT COUNT(*) FROM article_likes al WHERE al.article_id = article_id)")
    private Long articleTotalLikes;

    public Article() {
    }

    public Article(UUID articleId, String title, String content, boolean published, LocalDate createdAt, Profile profile, List<Tag> tags, List<Profile> articleLikes) {
        this.articleId = articleId;
        this.title = title;
        this.content = content;
        this.published = published;
        this.createdAt = createdAt;
        this.profile = profile;
        this.tags = tags;
        this.articleLikes = articleLikes;
    }

    public UUID getArticleId() {
        return articleId;
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

    public List<Profile> getArticleLikes() {
        return articleLikes;
    }

    public void setArticleLikes(List<Profile> articleLikes) {
        this.articleLikes = articleLikes;
    }

    public Long getArticleTotalLikes() {
        return articleTotalLikes == null ? 0 : articleTotalLikes;
    }
}
