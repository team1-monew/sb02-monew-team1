package com.team1.monew.subscription.event;

import lombok.Builder;

@Builder
public record SubscriptionDeleteEvent(
    Long subscriptionId,
    Long userId
) {
}
