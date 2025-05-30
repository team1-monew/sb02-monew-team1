package com.team1.monew.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.team1.monew.exception.RestException;
import com.team1.monew.user.dto.UserDto;
import com.team1.monew.user.dto.UserRegisterRequest;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.mapper.UserMapper;
import com.team1.monew.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
  @Mock
  UserRepository userRepository;

  @Mock
  UserMapper userMapper;

  @InjectMocks
  UserServiceImpl userService;

  private UserRegisterRequest request;
  private User user;
  private UserDto userDto;

  @BeforeEach
  void setUp() {
    request = new UserRegisterRequest(
        "user1@email.com",
        "user1",
        "user1@@@"
    );

    user = User.builder()
        .email(request.email())
        .nickname(request.nickname())
        .password(request.password())
        .build();

    ReflectionTestUtils.setField(user, "id", 1L);

    userDto = UserDto.builder()
        .id(user.getId())
        .email(user.getEmail())
        .nickname(user.getNickname())
        .createdAt(user.getCreatedAt())
        .build();
  }

  @Test
  @DisplayName("createUser() 성공")
  void createUser_saveUser_returnUserDto() {
    // given
    given(userRepository.existsByEmail(request.email())).willReturn(false);
    given(userRepository.save(any(User.class))).willReturn(user);
    given(userMapper.toDto(any(User.class))).willReturn(userDto);

    // when
    UserDto result = userService.createUser(request);

    // then
    assertThat(result).isEqualTo(userDto);
    verify(userRepository).save(any(User.class));
    verify(userMapper).toDto(any(User.class));
  }

  @Test
  @DisplayName("createUser() 실패 - 중복 이메일")
  void createUser_duplicateEmail_throwsException() {
    // given
    given(userRepository.existsByEmail(request.email())).willReturn(true);

    // when & then
    assertThrows(RestException.class, () -> userService.createUser(request));
  }

  @Test
  @DisplayName("중복 이메일 검증 시 예외 반환")
  void validateEmailNotDuplicated() {
    // given
    given(userRepository.existsByEmail(request.email())).willReturn(true);

    // when & then
    assertThrows(RestException.class, () ->
        userService.validateEmailNotDuplicated(request.email()));
  }
}