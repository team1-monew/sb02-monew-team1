package com.team1.monew.useractivity.dto;

import com.team1.monew.article.entity.ArticleView;
import com.team1.monew.comment.entity.Comment;
import com.team1.monew.comment.entity.CommentLike;
import com.team1.monew.subscription.entity.Subscription;
import com.team1.monew.user.entity.User;
import java.util.List;
import lombok.Builder;

@Builder
public record UserActivityParam(
    User user,
    List<Subscription> subscriptions,
    List<ArticleView> articleViews,
    List<Comment> recentComments,
    List<CommentLike> commentLikes
) {


}
