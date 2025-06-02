package com.team1.monew.article.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "articles")
@Builder
@AllArgsConstructor
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArticleInterest> articleInterests = new ArrayList<>();

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String sourceUrl;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime publishDate;

    @Column(length = 1000)
    private String summary;

    @Builder.Default
    @Column(nullable = false)
    private Long viewCount = 0L;

    @Builder.Default
    @Column(nullable = false)
    private boolean isDeleted = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void addArticleInterest(ArticleInterest articleInterest) {
        articleInterests.add(articleInterest);
        articleInterest.updateArticle(this);
    }

    public void markDeleted() {
        this.isDeleted = true;
    }

    public void restore() {
        this.isDeleted = false;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }
}
