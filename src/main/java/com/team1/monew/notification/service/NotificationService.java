package com.team1.monew.notification.service;

import com.team1.monew.comment.entity.Comment;
import com.team1.monew.common.dto.CursorPageResponse;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.notification.dto.NotificationCursorRequest;
import com.team1.monew.notification.dto.NotificationDto;
import com.team1.monew.user.entity.User;

public interface NotificationService {
  void notifyNewArticles(User user, Interest interest, int articleCount);
  void notifyCommentLiked(Comment comment, User likedBy);
  CursorPageResponse<NotificationDto> getAllNotifications(NotificationCursorRequest request);
  void confirmAll(Long userId);
  void confirm(Long notificationId);
}