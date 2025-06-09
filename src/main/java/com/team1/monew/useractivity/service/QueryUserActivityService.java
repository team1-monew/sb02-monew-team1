package com.team1.monew.useractivity.service;

import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import com.team1.monew.useractivity.dto.UserActivityDto;
import com.team1.monew.useractivity.entity.UserActivity;
import com.team1.monew.useractivity.mapper.UserActivityMapper;
import com.team1.monew.useractivity.repository.UserActivityRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@Primary
public class QueryUserActivityService implements UserActivityService{

  private final UserActivityRepository userActivityRepository;
  private final UserActivityMapper userActivityMapper;

  @Override
  public UserActivityDto findUserActivity(Long userId) {
    UserActivity userActivity = userActivityRepository.findByUser_Id(userId)
        .orElseThrow(() -> {
          log.warn("활동 내역 조회 실패 - 해당 유저가 존재하지 않음, userId: {}", userId);
          return new RestException(ErrorCode.NOT_FOUND,
              Map.of("userId", userId, "detail", "user not found"));
        });
    return userActivityMapper.toQueryDto(userActivity);
  }
}
