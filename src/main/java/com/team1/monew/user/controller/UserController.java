package com.team1.monew.user.controller;

import com.team1.monew.user.dto.UserDto;
import com.team1.monew.user.dto.UserLoginRequest;
import com.team1.monew.user.dto.UserRegisterRequest;
import com.team1.monew.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @PostMapping
  public ResponseEntity<UserDto> create(
      @RequestBody UserRegisterRequest userRegisterRequest
  ) {
    log.info("회원가입 요청: email={}, nickname={}", userRegisterRequest.email(), userRegisterRequest.nickname());
    UserDto createdUser = userService.createUser(userRegisterRequest);
    log.debug("회원가입 응답: {}", createdUser);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(createdUser);
  }
}
