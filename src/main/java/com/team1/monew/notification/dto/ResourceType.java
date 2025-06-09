package com.team1.monew.notification.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ResourceType {
  INTEREST("interest"),
  COMMENT("comment");

  private final String name;
}
