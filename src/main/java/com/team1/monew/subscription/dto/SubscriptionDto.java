package com.team1.monew.subscription.dto;

import java.time.Instant;
import java.util.List;

public record SubscriptionDto(
    Long id,
    Long interestId,
    String interestName,
    List<String> interestKeywords,
    Long interestSubscriberCount,
    Instant createdAt
) {

}
