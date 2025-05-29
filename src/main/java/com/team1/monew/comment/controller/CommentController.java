package com.team1.monew.comment.controller;

import com.team1.monew.comment.dto.CommentDto;
import com.team1.monew.comment.dto.CommentRegisterRequest;
import com.team1.monew.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@AllArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDto> create(@RequestBody @Valid CommentRegisterRequest request) {
        log.info("POST /api/comments 요청 수신 - articleId: {}, userId: {}", request.articleId(), request.userId());

        CommentDto commentDto = commentService.register(request);

        log.info("댓글 등록 요청 성공 - commentId: {}", commentDto.id());

        return ResponseEntity
                .status(201)
                .body(commentDto);
    }
}
