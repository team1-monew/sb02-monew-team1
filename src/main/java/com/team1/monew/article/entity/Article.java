package com.team1.monew.article.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor
@Table(name = "articles")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArticleInterest> articleInterests = new ArrayList<>();

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String sourceUrl;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Instant publishDate;

    @Column(length = 1000)
    private String summary;

    @Column(nullable = false)
    private Long viewCount = 0L;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Article(String source, String sourceUrl, String title, Instant publishDate, String summary) {
        this.source = source;
        this.sourceUrl = sourceUrl;
        this.title = title;
        this.publishDate = publishDate;
        this.summary = summary;
        this.createdAt = Instant.now();
    }
}
