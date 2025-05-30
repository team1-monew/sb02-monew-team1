package com.team1.monew.useractivity.dto;

import com.team1.monew.article.entity.ArticleView;
import com.team1.monew.comment.entity.Comment;
import com.team1.monew.comment.entity.CommentLike;
import com.team1.monew.interest.entity.Subscription;
import java.time.Instant;
import java.util.List;

public record UserActivityDto(
    Long id,
    String email,
    String nickname,
    Instant createdAt,
    List<Subscription> subscriptions,
    List<Comment> comments,
    List<CommentLike> commentLikes,
    List<ArticleView> articleViews
) {

}
