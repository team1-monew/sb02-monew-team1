package com.team1.monew.comment.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team1.monew.comment.dto.CommentOrderBy;
import com.team1.monew.comment.dto.CommentSearchCondition;
import com.team1.monew.comment.entity.Comment;
import com.team1.monew.comment.entity.QComment;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Comment> searchByCondition(CommentSearchCondition condition) {
        QComment comment = QComment.comment;

        BooleanBuilder where = new BooleanBuilder()
            .and(comment.article.id.eq(condition.articleId()))
            .and(comment.isDeleted.isFalse());

        // 커서 처리
        if (condition.cursor() != null) {
            if (condition.orderBy() == CommentOrderBy.CREATED_AT) {
                LocalDateTime cursorDateTime = LocalDateTime.parse(condition.cursor());
                where.and(condition.direction().isAscending()
                    ? comment.createdAt.gt(cursorDateTime)
                    : comment.createdAt.lt(cursorDateTime));
            } else if (condition.orderBy() == CommentOrderBy.LIKE_COUNT) {
                Long cursorLike = Long.parseLong(condition.cursor());
                LocalDateTime after = condition.after();

                if (condition.direction().isAscending()) {
                    where.and(
                        comment.likeCount.gt(cursorLike)
                            .or(comment.likeCount.eq(cursorLike)
                                .and(comment.createdAt.lt(after)))
                    );
                } else {
                    where.and(
                        comment.likeCount.lt(cursorLike)
                            .or(comment.likeCount.eq(cursorLike)
                                .and(comment.createdAt.lt(after)))
                    );
                }
            }
        }

        // 정렬 조건
        OrderSpecifier<?>[] orderSpecifiers = switch (condition.orderBy()) {
            case CREATED_AT -> new OrderSpecifier<?>[] {
                condition.direction().isAscending() ? comment.createdAt.asc() : comment.createdAt.desc()
            };
            case LIKE_COUNT -> new OrderSpecifier<?>[] {
                condition.direction().isAscending() ? comment.likeCount.asc() : comment.likeCount.desc(),
                comment.createdAt.desc()
            };
        };

        int limit = condition.limit() + 1;
        List<Comment> result = queryFactory
            .selectFrom(comment)
            .where(where)
            .orderBy(orderSpecifiers)
            .limit(limit)
            .fetch();

        boolean hasNext = result.size() > condition.limit();
        if (hasNext) {
            result.remove(result.size() - 1);
        }

        return new SliceImpl<>(result, PageRequest.of(0, condition.limit()), hasNext);
    }
}
