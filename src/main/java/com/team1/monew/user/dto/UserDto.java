package com.team1.monew.user.dto;

import java.time.Instant;

public record UserDto(
    Long id,
    String email,
    String nickname,
    Instant createdAt
) {

}
