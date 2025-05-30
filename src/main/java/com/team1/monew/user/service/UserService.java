package com.team1.monew.user.service;

import com.team1.monew.user.dto.UserDto;
import com.team1.monew.user.dto.UserLoginRequest;
import com.team1.monew.user.dto.UserRegisterRequest;

public interface UserService {
  UserDto createUser(UserRegisterRequest userRegisterRequest);
  UserDto login(UserLoginRequest userLoginRequest);
  void validateEmailNotDuplicated(String email);
}
