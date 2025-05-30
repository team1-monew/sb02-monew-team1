package com.team1.monew.interest.mapper;

import com.team1.monew.interest.dto.InterestDto;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.interest.entity.Keyword;
import org.springframework.stereotype.Component;

@Component
public class InterestMapper {
  public InterestDto toDto(Interest interest) {
    return InterestDto.builder()
        .id(interest.getId())
        .name(interest.getName())
        .keywords(interest.getKeywords().stream().map(Keyword::getKeyword).toList())
        .subscriberCount(interest.getSubscriberCount())
        .subscribedByMe(true) // 추후 subscription 관련 기능 만들고 수정
        .build();
  }
}
