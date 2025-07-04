package com.team1.monew.comment.entity;

import com.team1.monew.article.entity.Article;
import com.team1.monew.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name="comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Long likeCount = 0L;

    @Column(nullable = false)
    private boolean isDeleted = false;

    public Comment(Article article, User user, String content) {
        this.article = article;
        this.user = user;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = createdAt;
    }

    public void update(String newContent) {
        if (newContent != null && !newContent.equals(this.content)) {
            this.content = newContent;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void updateLikeCount(Long newLikeCount){
        if (newLikeCount != null) {
            this.likeCount = newLikeCount;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void delete() {
        this.isDeleted = true;
        this.updatedAt = LocalDateTime.now();
    }
}
