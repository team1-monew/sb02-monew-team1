package com.team1.monew.useractivity.service;

import com.team1.monew.article.entity.Article;
import com.team1.monew.article.entity.ArticleView;
import com.team1.monew.article.repository.ArticleRepository;
import com.team1.monew.article.repository.ArticleViewRepository;
import com.team1.monew.comment.entity.Comment;
import com.team1.monew.comment.entity.CommentLike;
import com.team1.monew.comment.repository.CommentLikeRepository;
import com.team1.monew.comment.repository.CommentRepository;
import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import com.team1.monew.subscription.entity.Subscription;
import com.team1.monew.subscription.repository.SubscriptionRepository;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import com.team1.monew.useractivity.dto.UserActivityDto;
import com.team1.monew.useractivity.dto.UserActivityParam;
import com.team1.monew.useractivity.mapper.UserActivityMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserActivityServiceImpl implements UserActivityService {

  private final SubscriptionRepository subscriptionRepository;
  private final CommentRepository commentRepository;
  private final CommentLikeRepository commentLikeRepository;
  private final ArticleRepository articleRepository;
  private final ArticleViewRepository articleViewRepository;
  private final UserRepository userRepository;

  private final UserActivityMapper userActivityMapper;


  @Transactional
  @Override
  public UserActivityDto findUserActivity(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> {
          log.warn("활동내역 조회 실패 - 해당 유저가 존재하지 않음, userId: {}", userId);
          return new RestException(ErrorCode.NOT_FOUND,
              Map.of("userId", userId, "detail", "user not found"));
        });
    List<Subscription> subscriptionList = subscriptionRepository.findByUserIdFetch(userId);
    List<ArticleView> articleViewList = articleViewRepository.findTop10ArticleViewByUserId(userId,
        PageRequest.of(0, 10));
    List<Comment> top10CommentByUserId = commentRepository.findTop10ByUser_IdAndIsDeletedFalseOrderByCreatedAtDesc(
        userId);
    List<CommentLike> top10CommentLikeByLike = commentLikeRepository.findWithCommentByLikedById(
        userId,
        PageRequest.of(0, 10));

    UserActivityParam userActivityParam = UserActivityParam.builder()
        .user(user)
        .articleViews(articleViewList)
        .commentLikes(top10CommentLikeByLike)
        .recentComments(top10CommentByUserId)
        .subscriptions(subscriptionList)
        .build();

    return userActivityMapper.toDto(userActivityParam);
  }
}
