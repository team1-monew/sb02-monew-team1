package com.team1.monew.comment.mapper;

import com.team1.monew.comment.CommentLikeRepository;
import com.team1.monew.comment.dto.CommentDto;
import com.team1.monew.comment.entity.Comment;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CommentMapper {

    private final CommentLikeRepository commentLikeRepository;

    public CommentDto toDto(Comment comment, Long userId) {
        boolean likedByMe = commentLikeRepository.existsByCommentIdAndUserId(
                comment.getId(),
                userId
        );
        return CommentDto.builder()
                .id(comment.getId())
                .articleId(comment.getArticle().getId())
                .userId(comment.getUser().getId())
                .userNickname(comment.getUser().getNickname())
                .content(comment.getContent())
                .likeCount(comment.getLikeCount())
                .likedByMe(likedByMe)
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
