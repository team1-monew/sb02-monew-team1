package com.team1.monew.article.entity;

import com.team1.monew.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "article_views")
public class ArticleView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User viewedBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public ArticleView(Article article, User viewedBy) {
        this.article = article;
        this.viewedBy = viewedBy;
        this.createdAt = Instant.now();
    }
}
