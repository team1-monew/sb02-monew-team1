package com.team1.monew.comment.repository;

import com.team1.monew.comment.dto.CommentSearchCondition;
import com.team1.monew.comment.entity.Comment;
import org.springframework.data.domain.Slice;

public interface CommentRepositoryCustom {
    Slice<Comment> searchByCondition(CommentSearchCondition condition);
}
