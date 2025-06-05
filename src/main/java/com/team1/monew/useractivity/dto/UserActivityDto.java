package com.team1.monew.useractivity.dto;

import com.team1.monew.article.dto.ArticleViewDto;
import com.team1.monew.comment.dto.CommentActivityDto;
import com.team1.monew.comment.dto.CommentLikeActivityDto;
import com.team1.monew.subscription.dto.SubscriptionDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record UserActivityDto(
    Long id,
    String email,
    String nickname,
    LocalDateTime createdAt,
    List<SubscriptionDto> subscriptions,
    List<CommentActivityDto> comments,
    List<CommentLikeActivityDto> commentLikes,
    List<ArticleViewDto> articleViews
) {

}
