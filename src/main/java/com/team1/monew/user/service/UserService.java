package com.team1.monew.user.service;

import com.team1.monew.user.dto.UserDto;
import com.team1.monew.user.dto.UserLoginRequest;
import com.team1.monew.user.dto.UserRegisterRequest;
import com.team1.monew.user.dto.UserUpdateRequest;

public interface UserService {
  UserDto createUser(UserRegisterRequest userRegisterRequest);
  UserDto login(UserLoginRequest userLoginRequest);
  UserDto updateUser(Long id, UserUpdateRequest userUpdateRequest);
  void deleteUser(Long id);
}
