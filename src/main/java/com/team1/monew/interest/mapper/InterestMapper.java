package com.team1.monew.interest.mapper;

import com.team1.monew.interest.dto.InterestDto;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.interest.entity.Keyword;
import org.springframework.stereotype.Component;

@Component
public class InterestMapper {
  public InterestDto toDto(Interest interest, boolean subscribedByMe) {
    return InterestDto.builder()
        .id(interest.getId())
        .name(interest.getName())
        .keywords(interest.getKeywords().stream().map(Keyword::getKeyword).toList())
        .createdAt(interest.getCreatedAt())
        .subscriberCount(interest.getSubscriberCount())
        .subscribedByMe(subscribedByMe)
        .build();
  }
}
