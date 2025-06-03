package com.team1.monew.interest.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record InterestDto(
    Long id,
    String name,
    List<String> keywords,
    Long subscriberCount,
    boolean subscribedByMe,
    LocalDateTime createdAt
) {

}
