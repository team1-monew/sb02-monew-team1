package com.team1.monew.comment.service;

import com.team1.monew.article.entity.Article;
import com.team1.monew.article.repository.ArticleRepository;
import com.team1.monew.comment.repository.CommentRepository;
import com.team1.monew.comment.dto.CommentDto;
import com.team1.monew.comment.dto.CommentRegisterRequest;
import com.team1.monew.comment.entity.Comment;
import com.team1.monew.comment.mapper.CommentMapper;
import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;


    @Override
    public CommentDto register(CommentRegisterRequest request) {
        log.info("댓글 등록 요청 - articleId: {}, userId: {}", request.articleId(), request.userId());

        User user = userRepository.findById(request.userId())
            .orElseThrow(() -> {
                log.warn("사용자 조회 실패 - userId: {}", request.userId());
                return new RestException(ErrorCode.NOT_FOUND, Map.of(
                    "userId", request.userId(),
                    "detail", "User not found"
                ));
            });

        Article article = articleRepository.findById(request.articleId())
            .orElseThrow(() -> {
                log.warn("기사 조회 실패 - articleId: {}", request.articleId());
                return new RestException(ErrorCode.NOT_FOUND, Map.of(
                    "articleId", request.articleId(),
                    "detail", "Article not found"
                ));
            });

        Comment newComment = Comment.builder()
            .content(request.content())
            .user(user)
            .article(article)
            .build();

        Comment savedComment = commentRepository.save(newComment);
        log.info("댓글 저장 성공 - commentId: {}", savedComment.getId());

        CommentDto dto = commentMapper.toDto(savedComment, request.userId());
        log.debug("댓글 DTO 반환 - {}", dto);

        return dto;
    }
}
