package com.team1.monew.user.mapper;

import com.team1.monew.user.dto.UserDto;
import com.team1.monew.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
  public UserDto toDto(User user) {
    return UserDto.builder()
        .id(user.getId())
        .email(user.getEmail())
        .nickname(user.getNickname())
        .createdAt(user.getCreatedAt())
        .build();
  }
}
