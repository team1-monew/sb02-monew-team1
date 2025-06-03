package com.team1.monew.interest.dto;


import java.time.LocalDateTime;
import lombok.Builder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Builder
public record InterestSearchCondition(
    String keyword,
    String cursor,
    LocalDateTime after,
    int limit,
    String orderBy,
    String direction
) {
  public Pageable toPageable() {
    Sort sort = "subscriberCount".equalsIgnoreCase(orderBy)
        ? Sort.by(Sort.Direction.fromString(direction), "subscriberCount")
        : Sort.by(Sort.Direction.fromString(direction), "name");

    return PageRequest.of(0, limit, sort);
  }
}
