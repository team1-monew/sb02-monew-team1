package com.team1.monew.comment.mapper;

import com.team1.monew.comment.dto.CommentActivityDto;
import com.team1.monew.comment.dto.CommentDto;
import com.team1.monew.comment.entity.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentDto toDto(Comment comment, boolean likedByMe) {

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

    public CommentActivityDto toActivityDto(Comment comment) {

        return CommentActivityDto.builder()
            .id(comment.getId())
            .articleId(comment.getArticle().getId())
            .articleTitle(comment.getArticle().getTitle())
            .userId(comment.getUser().getId())
            .userNickname(comment.getUser().getNickname())
            .content(comment.getContent())
            .likeCount(comment.getLikeCount())
            .createdAt(comment.getCreatedAt())
            .build();
    }
}
