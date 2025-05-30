package com.team1.monew.user.service;

import com.team1.monew.user.dto.UserDto;
import com.team1.monew.user.dto.UserRegisterRequest;

public interface UserService {
  UserDto createUser(UserRegisterRequest userRegisterRequest);
  void validateEmailNotDuplicated(String email);
}
