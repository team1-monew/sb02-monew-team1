package com.team1.monew.subscription.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record SubscriptionDto(
    Long id,
    Long interestId,
    String interestName,
    List<String> interestKeywords,
    Long interestSubscriberCount,
    LocalDateTime createdAt
) {

}
