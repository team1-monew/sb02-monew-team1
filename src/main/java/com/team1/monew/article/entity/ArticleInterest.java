package com.team1.monew.article.entity;

import com.team1.monew.interest.entity.Interest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "article_interests")
public class ArticleInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interest_id", nullable = false)
    private Interest interest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    public ArticleInterest(Interest interest, Article article) {
        this.interest = interest;
        this.article = article;
    }

    public void updateArticle(Article article) {
        this.article = article;
    }

    public void updateInterest(Interest interest) {
        this.interest = interest;
    }
}