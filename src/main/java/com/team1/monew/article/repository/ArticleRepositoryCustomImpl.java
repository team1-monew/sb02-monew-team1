package com.team1.monew.article.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team1.monew.article.dto.ArticleDto;
import com.team1.monew.article.entity.*;
import com.team1.monew.article.mapper.ArticleMapper;
import com.team1.monew.comment.entity.QComment;
import com.team1.monew.common.dto.CursorPageResponse;
import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ArticleRepositoryCustomImpl implements ArticleRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private final QArticle article = QArticle.article;
    private final QComment comment = QComment.comment;
    private final QArticleView articleView = QArticleView.articleView;
    private final QArticleInterest articleInterest = QArticleInterest.articleInterest;

    @Override
    public CursorPageResponse<ArticleDto> searchArticles(
            String keyword,
            Long interestId,
            List<String> sourceIn,
            LocalDateTime publishDateFrom,
            LocalDateTime publishDateTo,
            String orderBy,
            String direction,
            String cursor,
            int limit,
            String after,
            Long userId
    ) {
        BooleanBuilder where = new BooleanBuilder();

        where.and(article.isDeleted.eq(false));

        if (keyword != null && !keyword.isBlank()) {
            where.and(
                    article.title.containsIgnoreCase(keyword)
                            .or(article.summary.containsIgnoreCase(keyword))
            );
        }

        if (interestId != null) {
            where.and(article.id.in(
                    JPAExpressions.select(articleInterest.article.id)
                            .from(articleInterest)
                            .where(articleInterest.interest.id.eq(interestId))
            ));
        }

        if (sourceIn != null && !sourceIn.isEmpty()) {
            where.and(article.source.in(sourceIn));
        }

        if (publishDateFrom != null) {
            where.and(article.publishDate.goe(publishDateFrom));
        }

        if (publishDateTo != null) {
            where.and(article.publishDate.loe(publishDateTo));
        }

        boolean asc = "ASC".equalsIgnoreCase(direction);

        if (cursor != null && !cursor.isBlank() && after != null && !after.isBlank()) {
            try {
                LocalDateTime cursorDateTime = LocalDateTime.parse(cursor);
                LocalDateTime afterCreatedAt = LocalDateTime.parse(after);

                BooleanExpression cursorCondition;
                if (asc) {
                    cursorCondition = article.publishDate.gt(cursorDateTime)
                            .or(article.publishDate.eq(cursorDateTime).and(article.createdAt.gt(afterCreatedAt)));
                } else {
                    cursorCondition = article.publishDate.lt(cursorDateTime)
                            .or(article.publishDate.eq(cursorDateTime).and(article.createdAt.lt(afterCreatedAt)));
                }
                where.and(cursorCondition);
            } catch (DateTimeParseException e) {
                throw new RestException(ErrorCode.INVALID_INPUT_VALUE, Map.of(
                        "reason", "Invalid cursor or after datetime format",
                        "cursor", cursor,
                        "after", after
                ));
            }
        }

        NumberExpression<Long> commentCountExpr = comment.count().coalesce(0L);

        List<Tuple> tuples = queryFactory
                .select(article, commentCountExpr)
                .from(article)
                .leftJoin(comment).on(comment.article.eq(article).and(comment.isDeleted.eq(false)))
                .where(where)
                .groupBy(article.id)
                .orderBy(getOrderSpecifiers(orderBy, asc, article, commentCountExpr))
                .limit(limit + 1)
                .fetch();

        boolean hasNext = tuples.size() > limit;
        if (hasNext) {
            tuples.remove(limit);
        }

        List<Article> articles = tuples.stream().map(t -> t.get(article)).toList();

        Map<Long, Long> commentCountMap = tuples.stream()
                .collect(Collectors.toMap(
                t -> t.get(article).getId(),
                t -> t.get(commentCountExpr)
        ));

        Set<Long> viewedArticleIds = new HashSet<>();
        if (userId != null && !articles.isEmpty()) {
            List<Long> articleIds = articles.stream().map(Article::getId).toList();
            viewedArticleIds.addAll(
                    queryFactory
                            .select(articleView.article.id)
                            .from(articleView)
                            .where(articleView.article.id.in(articleIds)
                                    .and(articleView.viewedBy.id.eq(userId)))
                            .fetch()
            );
        }

        List<ArticleDto> content = articles.stream()
                .map(a -> {
                    Long cCount = commentCountMap.getOrDefault(a.getId(), 0L);
                    boolean viewedByMe = viewedArticleIds.contains(a.getId());
                    return ArticleMapper.toDto(a, cCount, viewedByMe);
                }).toList();

        String nextCursor = null;
        LocalDateTime nextAfter = null;
        if (!articles.isEmpty()) {
            Article last = articles.get(articles.size() - 1);
            nextCursor = last.getPublishDate().toString();
            nextAfter = last.getCreatedAt();
        }

        long totalElements = Optional.ofNullable(
                queryFactory
                        .select(article.id.countDistinct())
                        .from(article)
                        .leftJoin(comment).on(comment.article.eq(article).and(comment.isDeleted.eq(false)))
                        .where(where)
                        .fetchOne()
        ).orElse(0L);

        return new CursorPageResponse<>(content, nextCursor, nextAfter, (long) limit, totalElements, hasNext);
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(String orderBy, boolean asc, QArticle article, NumberExpression<Long> commentCountExpr) {
        return switch (orderBy.toLowerCase()) {
            case "viewcount" -> new OrderSpecifier[]{
                    asc ? article.viewCount.asc() : article.viewCount.desc(),
                    asc ? article.id.asc() : article.id.desc()
            };
            case "commentcount" -> new OrderSpecifier[]{
                    asc ? commentCountExpr.asc() : commentCountExpr.desc(),
                    asc ? article.id.asc() : article.id.desc()
            };
            default -> new OrderSpecifier[]{
                    asc ? article.publishDate.asc() : article.publishDate.desc(),
                    asc ? article.id.asc() : article.id.desc()
            };
        };
    }

    @Override
    public List<ArticleDto> findAllCreatedYesterday() {
        LocalDateTime start = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime end = LocalDate.now().minusDays(1).atTime(23, 59, 59, 999999999);

        List<Article> articles = queryFactory
                .selectFrom(article)
                .where(article.createdAt.between(start, end))
                .fetch();

        return articles.stream()
                .map(a -> ArticleMapper.toDto(a, 0L, false))
                .toList();
    }
}