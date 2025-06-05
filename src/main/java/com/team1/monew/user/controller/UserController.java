package com.team1.monew.user.controller;

import com.team1.monew.user.dto.UserDto;
import com.team1.monew.user.dto.UserLoginRequest;
import com.team1.monew.user.dto.UserRegisterRequest;
import com.team1.monew.user.dto.UserUpdateRequest;
import com.team1.monew.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
      @RequestBody @Valid UserRegisterRequest userRegisterRequest
  ) {
    log.info("회원가입 요청: email={}, nickname={}", userRegisterRequest.email(), userRegisterRequest.nickname());
    UserDto createdUser = userService.createUser(userRegisterRequest);
    log.debug("회원가입 응답: {}", createdUser);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(createdUser);
  }

  @PostMapping("/login")
  public ResponseEntity<UserDto> login(
      @RequestBody @Valid UserLoginRequest userLoginRequest
  ) {
    log.info("로그인 요청: email={}", userLoginRequest.email());
    UserDto createdUser = userService.login(userLoginRequest);
    log.debug("로그인 응답: {}", createdUser);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(createdUser);
  }

  @PatchMapping("/{userId}")
  public ResponseEntity<UserDto> update(
      @PathVariable Long userId,
      @RequestBody @Valid UserUpdateRequest userUpdateRequest
  ) {
    log.info("사용자 수정 요청: id={}, request={}", userId, userUpdateRequest);
    UserDto updatedUser = userService.updateUser(userId, userUpdateRequest);
    log.debug("사용자 수정 응답: {}", updatedUser);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(updatedUser);
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> delete(
      @PathVariable Long userId
  ) {
    log.info("사용자 논리 삭제 요청: id={}", userId);
    userService.deleteUser(userId);
    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }

  @DeleteMapping("/{userId}/hard")
  public ResponseEntity<Void> deleteHard(
      @PathVariable Long userId
  ) {
    log.info("사용자 물리 삭제 요청: id={}", userId);
    userService.deleteUserHard(userId);
    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }
}
