package com.team1.monew.interest.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SubscriptionDto(
    Long id,
    Long interestId,
    String interestName,
    List<String> interestKeywords,
    Long interestSubscriberCount,
    LocalDateTime createdAt
) {

}
