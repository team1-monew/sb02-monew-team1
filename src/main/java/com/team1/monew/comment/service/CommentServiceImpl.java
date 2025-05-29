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
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;


    @Override
    public CommentDto register(CommentRegisterRequest request) {

        User user = userRepository.findById(request.userId())
            .orElseThrow(() -> new RestException(ErrorCode.NOT_FOUND, Map.of("userId", request.userId())));

        Article article = articleRepository.findById(request.articleId())
            .orElseThrow(() -> new RestException(ErrorCode.NOT_FOUND, Map.of("articleId", request.articleId())));

        Comment newComment = Comment.builder()
            .content(request.content())
            .user(user)
            .article(article)
            .build();

        Comment savedComment = commentRepository.save(newComment);

        return commentMapper.toDto(savedComment, request.userId());
    }
}
