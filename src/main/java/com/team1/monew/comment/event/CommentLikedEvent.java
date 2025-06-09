package com.team1.monew.comment.event;

import com.team1.monew.comment.entity.Comment;
import com.team1.monew.user.entity.User;
import lombok.Getter;

@Getter
public class CommentLikedEvent {
  private final Comment comment;      // 좋아요가 눌린 댓글
  private final User likedBy;         //

  public CommentLikedEvent(Comment comment, User likedBy) {
    this.comment = comment;
    this.likedBy = likedBy;
  }
}
