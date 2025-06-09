package com.team1.monew.notification.service;

import com.team1.monew.comment.entity.Comment;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.user.entity.User;

public interface NotificationService {
  void notifyNewArticles(User user, Interest interest, int articleCount);
  void notifyCommentLiked(Comment comment, User likedBy);
}
