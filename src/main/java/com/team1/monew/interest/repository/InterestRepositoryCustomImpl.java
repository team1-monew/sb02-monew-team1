package com.team1.monew.interest.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team1.monew.interest.dto.InterestSearchCondition;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.interest.entity.QInterest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class InterestRepositoryCustomImpl implements InterestRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Slice<Interest> searchByCondition(InterestSearchCondition condition) {
    QInterest interest = QInterest.interest;

    BooleanBuilder where = new BooleanBuilder();
    // 검색 필터 조건
    if (StringUtils.hasText(condition.keyword())) {
      where.and(
          interest.name.containsIgnoreCase(condition.keyword())
              .or(interest.keywords.any().keyword.containsIgnoreCase(condition.keyword()))
      );
    }
    // 커서 조건 (같은 경우엔 보조 커서인 after를 이용)
    if (StringUtils.hasText(condition.cursor())) {
      if ("subscriberCount".equalsIgnoreCase(condition.orderBy())) {
        Long cursorValue = Long.parseLong(condition.cursor());
        where.and("ASC".equalsIgnoreCase(condition.direction())
            ? interest.subscriberCount.gt(cursorValue)
            .or(interest.subscriberCount.eq(cursorValue)
                .and(interest.createdAt.gt(condition.after())))
            : interest.subscriberCount.lt(cursorValue)
                .or(interest.subscriberCount.eq(cursorValue)
                    .and(interest.createdAt.gt(condition.after()))));
      } else {
        String cursorValue = condition.cursor();
        where.and("ASC".equalsIgnoreCase(condition.direction())
            ? interest.name.gt(cursorValue)
            .or(interest.name.eq(cursorValue)
                .and(interest.createdAt.gt(condition.after())))
            : interest.name.lt(cursorValue)
                .or(interest.name.eq(cursorValue)
                    .and(interest.createdAt.gt(condition.after()))));
      }
    }

    // 정렬 조건 (같은 경우엔 createdAt을 기준으로 정렬)
    Order direction = "ASC".equalsIgnoreCase(condition.direction()) ? Order.ASC : Order.DESC;
    OrderSpecifier<?>[] orderSpecifiers = "subscriberCount".equalsIgnoreCase(condition.orderBy())
        ? new OrderSpecifier[]{
        new OrderSpecifier<>(direction, interest.subscriberCount),
        new OrderSpecifier<>(Order.ASC, interest.createdAt)
    }
        : new OrderSpecifier[]{
            new OrderSpecifier<>(direction, interest.name),
            new OrderSpecifier<>(Order.ASC, interest.createdAt)
        };

    // 검색 필터 조건 + 커서조건 + 정렬 조건을 모두 합친 조건을 만족하는 데이터를 뽑아내는 쿼리
    List<Interest> results = queryFactory
        .selectFrom(interest)
        .where(where)
        .orderBy(orderSpecifiers)
        .limit(condition.limit() + 1)
        .fetch();

    boolean hasNext = results.size() > condition.limit();
    List<Interest> content = hasNext ? results.subList(0, condition.limit()) : results;

    return new SliceImpl<>(content, condition.toPageable(), hasNext);
  }
}

