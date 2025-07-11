package com.team1.monew.comment.controller;

import com.team1.monew.comment.controller.api.CommentApi;
import com.team1.monew.comment.dto.CommentDto;
import com.team1.monew.comment.dto.CommentLikeDto;
import com.team1.monew.comment.dto.CommentRegisterRequest;
import com.team1.monew.comment.dto.CommentSearchCondition;
import com.team1.monew.comment.dto.CommentUpdateRequest;
import com.team1.monew.comment.service.CommentLikeService;
import com.team1.monew.comment.service.CommentService;
import com.team1.monew.common.dto.CursorPageResponse;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@AllArgsConstructor
@Slf4j
public class CommentController implements CommentApi {

    private final CommentService commentService;
    private final CommentLikeService commentLikeService;

    @PostMapping
    public ResponseEntity<CommentDto> create(@RequestBody @Valid CommentRegisterRequest request) {
        log.info("POST /api/comments 요청 수신 - articleId: {}, userId: {}", request.articleId(), request.userId());

        CommentDto commentDto = commentService.register(request);

        log.info("댓글 등록 요청 성공 - commentId: {}", commentDto.id());

        return ResponseEntity
                .status(201)
                .body(commentDto);
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto> update(
            @RequestBody @Valid CommentUpdateRequest request,
            @PathVariable Long commentId,
            @RequestHeader("Monew-Request-User-ID") Long userId
        ) {
        log.info("PATCH /api/comments/{} 요청 수신 - request.content: {}, userId: {}", commentId, request.content(), userId);

        CommentDto updatedCommentDto = commentService.update(request, commentId, userId);

        log.info("댓글 수정 요청 성공 - commentId: {}", updatedCommentDto.id());

        return ResponseEntity
                .ok(updatedCommentDto);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> softDelete(@PathVariable Long commentId, @RequestHeader("Monew-Request-User-ID") Long userId) {
        log.info("DELETE /api/comments/{} 요청 수신 - userId: {}", commentId, userId);

        commentService.softDelete(commentId, userId);

        log.info("댓글 삭제 요청 성공 - commentId: {}", commentId);

        return ResponseEntity
                .noContent()
                .build();
    }

    @DeleteMapping("/{commentId}/hard")
    public ResponseEntity<Void> hardDelete(@PathVariable Long commentId) {
        log.info("DELETE /api/comments/{}/hard 요청 수신", commentId);

        commentService.hardDelete(commentId);

        log.info("댓글 삭제 요청 성공 - commentId: {}", commentId);

        return ResponseEntity
            .noContent()
            .build();
    }

    @PostMapping("/{commentId}/comment-likes")
    public ResponseEntity<CommentLikeDto> like(@PathVariable Long commentId, @RequestHeader("Monew-Request-User-ID") Long userId) {
        log.info("POST /api/comments/{}/comment-likes 요청 수신 - commentId: {}, userId: {}", commentId, userId);

        CommentLikeDto commentLikeDto = commentLikeService.like(commentId, userId);

        log.info("댓글 좋아요 요청 성공 - commentId: {}, userId: {}", commentId, userId);

        return ResponseEntity
                .ok()
                .body(commentLikeDto);
    }

    @DeleteMapping("/{commentId}/comment-likes")
    public ResponseEntity<Void> unlike(@PathVariable Long commentId, @RequestHeader("Monew-Request-User-ID") Long userId) {
        log.info("DELETE /api/comments/{}/comment-likes 요청 수신 - commentId: {}, userId: {}", commentId, userId);

        commentLikeService.unlike(commentId, userId);

        log.info("댓글 좋아요 취소 요청 성공 - commentId: {}, userId: {}", commentId, userId);

        return ResponseEntity
                .ok()
                .build();
    }

    @GetMapping
    public ResponseEntity<CursorPageResponse<CommentDto>> findCommentsByArticleId(
            @RequestParam Long articleId,
            @RequestParam String orderBy,
            @RequestParam String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) LocalDateTime after,
            @RequestParam(defaultValue = "10") int limit,
            @RequestHeader("Monew-Request-User-ID") Long userId
        ) {
        log.debug("GET /api/comments 요청 수신 - articleId: {}, userId: {}", articleId, userId);

        CommentSearchCondition condition = CommentSearchCondition.fromParams(
            articleId, orderBy, direction, cursor, after, limit, userId
        );

        CursorPageResponse<CommentDto> comments = commentService.findCommentsByArticleId(condition);

        log.debug("댓글 조회 요청 성공 - nextCursor: {}, hasNext: {}", comments.nextCursor(), comments.hasNext());

        return ResponseEntity
                .ok(comments);
    }
}
