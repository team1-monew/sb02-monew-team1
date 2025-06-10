package com.team1.monew.useractivity.mapper;

import com.team1.monew.article.dto.ArticleViewDto;
import com.team1.monew.article.mapper.ArticleViewMapper;
import com.team1.monew.comment.dto.CommentActivityDto;
import com.team1.monew.comment.dto.CommentLikeActivityDto;
import com.team1.monew.comment.mapper.CommentLikeMapper;
import com.team1.monew.comment.mapper.CommentMapper;
import com.team1.monew.subscription.dto.SubscriptionDto;
import com.team1.monew.subscription.mapper.SubscriptionMapper;
import com.team1.monew.user.entity.User;
import com.team1.monew.useractivity.dto.UserActivityDto;
import com.team1.monew.useractivity.dto.UserActivityParam;
import com.team1.monew.useractivity.entity.UserActivity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserActivityMapper {
  private final CommentMapper commentMapper;
  private final CommentLikeMapper commentLikeMapper;
  private final SubscriptionMapper subscriptionMapper;


  public UserActivityDto toQueryDto(UserActivity userActivity){
    return UserActivityDto.builder()
        .id(userActivity.getId())
        .email(userActivity.getUser().email())
        .nickname(userActivity.getUser().nickname())
        .createdAt(userActivity.getUser().createdAt())
        .subscriptions(userActivity.getSubscriptionList())
        .comments(userActivity.getCommentList())
        .commentLikes(userActivity.getCommentLikeList())
        .articleViews(userActivity.getArticleViewList())
        .build();
  }

  public UserActivityDto toDto(UserActivityParam userActivityParam) {

    List<SubscriptionDto> subscriptionDtoList = userActivityParam.subscriptions().stream()
        .map(subscriptionMapper::toDto).toList();

    List<ArticleViewDto> articleViewDtoList = userActivityParam.articleViews().stream()
        .map(articleView -> ArticleViewMapper.toDto(articleView, 0L)).toList();

    List<CommentActivityDto> commentDtoList = userActivityParam.recentComments().stream()
        .map(commentMapper::toActivityDto).toList();

    List<CommentLikeActivityDto> commentLikeDtoList = userActivityParam.commentLikes().stream()
        .map(commentLikeMapper::toActivityDto).toList();

    User user = userActivityParam.user();

    return  UserActivityDto.builder()
        .id(user.getId())
        .articleViews(articleViewDtoList)
        .commentLikes(commentLikeDtoList)
        .email(user.getEmail())
        .nickname(user.getNickname())
        .comments(commentDtoList)
        .subscriptions(subscriptionDtoList)
        .createdAt(user.getCreatedAt())
        .build();
  }
}
