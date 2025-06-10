package com.team1.monew.notification.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team1.monew.notification.dto.NotificationCursorRequest;
import com.team1.monew.notification.dto.NotificationDto;
import com.team1.monew.notification.entity.QNotification;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {
  private final JPAQueryFactory queryFactory;

  @Override
  public Slice<NotificationDto> getAllByCursorRequest(NotificationCursorRequest request) {
    QNotification qNotification = QNotification.notification;

    // userId 처리
    BooleanBuilder where = new BooleanBuilder()
        .and(qNotification.confirmed.isFalse())
        .and(qNotification.user.id.eq(request.userId()));

    // cursor 처리
    if (request.cursor() != null) {
      LocalDateTime cursorDateTime = LocalDateTime.parse(request.cursor());
      where.and(request.direction().isAscending() ?
          qNotification.createdAt.gt(cursorDateTime) : qNotification.createdAt.lt(cursorDateTime));
    }

    // after 처리
    if (request.after() != null) {
      where.and(request.direction().isAscending() ?
          qNotification.createdAt.gt(request.after()) : qNotification.createdAt.lt(request.after()));
    }

    // direction 처리 - 정렬 조건
    OrderSpecifier<?>[] orders = new OrderSpecifier<?>[] {
        request.direction().isAscending() ? qNotification.createdAt.asc() : qNotification.createdAt.desc()
    };

    // limit 처리
    int limit = request.limit() + 1;
    List<NotificationDto> result = queryFactory
        .select(Projections.constructor(NotificationDto.class,
            qNotification.id,
            qNotification.createdAt,
            qNotification.updatedAt,
            qNotification.confirmed,
            qNotification.user.id,
            qNotification.content,
            qNotification.resourceType,
            qNotification.resourceId
            ))
        .from(qNotification)
        .join(qNotification.user)
        .where(where)
        .orderBy(orders)
        .limit(limit)
        .fetch();

    boolean hasNext = result.size() > request.limit();
    if (hasNext) {
      result.remove(result.size() - 1);
    }

    return new SliceImpl<>(result, PageRequest.of(0, request.limit()), hasNext);
  }
}
