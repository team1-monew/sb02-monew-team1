package com.team1.monew.subscription.mapper;

import com.team1.monew.interest.entity.Keyword;
import com.team1.monew.subscription.dto.SubscriptionDto;
import com.team1.monew.subscription.entity.Subscription;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {
  public SubscriptionDto toDto(Subscription subscription) {
    return SubscriptionDto.builder()
        .id(subscription.getId())
        .interestId(subscription.getInterest().getId())
        .createdAt(subscription.getCreatedAt())
        .interestKeywords(subscription.getInterest().getKeywords().stream().map(Keyword::getKeyword).toList())
        .interestName(subscription.getInterest().getName())
        .interestSubscriberCount(subscription.getInterest().getSubscriberCount())
        .build();
  }
}
