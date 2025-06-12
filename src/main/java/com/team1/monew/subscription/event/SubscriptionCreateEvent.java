package com.team1.monew.subscription.event;

import com.team1.monew.subscription.dto.SubscriptionDto;
import lombok.Builder;

@Builder
public record SubscriptionCreateEvent(
    Long userId,
    SubscriptionDto subscriptionDto
) {
}
