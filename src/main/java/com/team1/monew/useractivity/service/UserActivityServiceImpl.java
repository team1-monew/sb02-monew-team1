package com.team1.monew.useractivity.service;

import com.team1.monew.article.dto.ArticleViewDto;
import com.team1.monew.comment.dto.CommentActivityDto;
import com.team1.monew.comment.dto.CommentLikeActivityDto;
import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import com.team1.monew.subscription.dto.SubscriptionDto;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import com.team1.monew.useractivity.document.ArticleViewActivity;
import com.team1.monew.useractivity.document.CommentActivity;
import com.team1.monew.useractivity.document.CommentLikeActivity;
import com.team1.monew.useractivity.document.SubscriptionActivity;
import com.team1.monew.useractivity.dto.UserActivityDto;
import com.team1.monew.useractivity.repository.ArticleViewActivityRepository;
import com.team1.monew.useractivity.repository.CommentActivityRepository;
import com.team1.monew.useractivity.repository.CommentLikeActivityRepository;
import com.team1.monew.useractivity.repository.SubscriptionActivityRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
@Primary
public class UserActivityServiceImpl implements UserActivityService{

    private final CommentActivityRepository commentActivityRepository;
    private final CommentLikeActivityRepository commentLikeActivityRepository;
    private final ArticleViewActivityRepository articleViewActivityRepository;
    private final SubscriptionActivityRepository subscriptionActivityRepository;
    private final UserRepository userRepository;

    @Override
    public UserActivityDto findUserActivity(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("활동 내역 조회 실패 - 해당 유저가 존재하지 않음, userId: {}", userId);
        return new RestException(ErrorCode.NOT_FOUND,
            Map.of("userId", userId, "detail", "user not found"));
            });

        List<CommentActivityDto> comments = commentActivityRepository.findTop10CommentsByUserId(userId)
            .map(CommentActivity::getComments)
            .orElse(Collections.emptyList());

        List<CommentLikeActivityDto> commentLikes = commentLikeActivityRepository.findTop10CommentLikesByUserId(userId)
            .map(CommentLikeActivity::getCommentLikes)
            .orElse(Collections.emptyList());

        List<SubscriptionDto> subscriptions = subscriptionActivityRepository.findTop10SubscriptionsByUserId(userId)
            .map(SubscriptionActivity::getSubscriptions)
            .orElse(Collections.emptyList());

        List<ArticleViewDto> articleViews = articleViewActivityRepository.findTop10ArticleViewsByUserId(userId)
            .map(ArticleViewActivity::getArticleViews)
            .orElse(Collections.emptyList());

        return UserActivityDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .articleViews(articleViews)
            .commentLikes(commentLikes)
            .subscriptions(subscriptions)
            .comments(comments)
            .createdAt(LocalDateTime.now())
            .build();
    }
}
