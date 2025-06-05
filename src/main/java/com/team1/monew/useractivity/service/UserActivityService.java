package com.team1.monew.useractivity.service;

import com.team1.monew.useractivity.dto.UserActivityDto;

public interface UserActivityService {
  UserActivityDto findUserActivity(Long userId);
}
