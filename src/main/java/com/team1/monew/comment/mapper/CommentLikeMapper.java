package com.team1.monew.comment.mapper;

import com.team1.monew.comment.dto.CommentLikeActivityDto;
import com.team1.monew.comment.dto.CommentLikeDto;
import com.team1.monew.comment.entity.CommentLike;
import org.springframework.stereotype.Component;

@Component
public class CommentLikeMapper {
    public CommentLikeDto toDto(CommentLike commentLike, Long commentLikeCount) {

        return CommentLikeDto.builder()
            .id(commentLike.getId())
            .likedBy(commentLike.getLikedBy().getId())
            .createdAt(commentLike.getCreatedAt())
            .commentId(commentLike.getComment().getId())
            .articleId(commentLike.getComment().getArticle().getId())
            .commentUserId(commentLike.getComment().getUser().getId())
            .commentUserNickname(commentLike.getComment().getUser().getNickname())
            .commentContent(commentLike.getComment().getContent())
            .commentLikeCount(commentLikeCount)
            .commentCreatedAt(commentLike.getComment().getCreatedAt())
            .build();
    }

    public CommentLikeActivityDto toActivityDto(CommentLike commentLike) {

        return CommentLikeActivityDto.builder()
            .id(commentLike.getId())
            .createdAt(commentLike.getCreatedAt())
            .commentId(commentLike.getComment().getId())
            .articleId(commentLike.getComment().getArticle().getId())
            .articleTitle(commentLike.getComment().getArticle().getTitle())
            .commentUserId(commentLike.getComment().getUser().getId())
            .commentUserNickname(commentLike.getComment().getUser().getNickname())
            .commentContent(commentLike.getComment().getContent())
            .commentLikeCount(commentLike.getComment().getLikeCount())
            .commentCreatedAt(commentLike.getComment().getCreatedAt())
            .build();
    }
}
