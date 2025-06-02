package com.team1.monew.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.team1.monew.exception.RestException;
import com.team1.monew.user.dto.UserDto;
import com.team1.monew.user.dto.UserLoginRequest;
import com.team1.monew.user.dto.UserRegisterRequest;
import com.team1.monew.user.dto.UserUpdateRequest;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.mapper.UserMapper;
import com.team1.monew.user.repository.UserRepository;
import java.util.Optional;
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

  @Mock
  User deletedUser;

  @InjectMocks
  UserServiceImpl userService;

  private UserRegisterRequest userRegisterRequest;
  private UserLoginRequest userLoginRequest;
  private UserUpdateRequest userUpdateRequest;
  private User user;
  private UserDto userDto;
  private UserDto updatedUserDto;

  @BeforeEach
  void setUp() {
    userRegisterRequest = UserRegisterRequest.builder()
        .email("user1@email.com")
        .nickname("user1")
        .password("user1@@@")
        .build();

    userLoginRequest = UserLoginRequest.builder()
        .email("user1@email.com")
        .password("user1@@@")
        .build();

    userUpdateRequest = UserUpdateRequest.builder()
        .nickname("updatedUser1")
        .build();

    user = User.builder()
        .email(userRegisterRequest.email())
        .nickname(userRegisterRequest.nickname())
        .password(userRegisterRequest.password())
        .build();

    ReflectionTestUtils.setField(user, "id", 1L);

    userDto = UserDto.builder()
        .id(user.getId())
        .email(user.getEmail())
        .nickname(user.getNickname())
        .createdAt(user.getCreatedAt())
        .build();

    updatedUserDto = UserDto.builder()
        .id(user.getId())
        .email(user.getEmail())
        .nickname(userUpdateRequest.nickname())
        .createdAt(user.getCreatedAt())
        .build();
  }

  @Test
  @DisplayName("createUser() 성공")
  void createUser_saveUser_returnUserDto() {
    // given
    given(userRepository.existsByEmail(userRegisterRequest.email())).willReturn(false);
    given(userRepository.save(any(User.class))).willReturn(user);
    given(userMapper.toDto(any(User.class))).willReturn(userDto);

    // when
    UserDto result = userService.createUser(userRegisterRequest);

    // then
    assertThat(result).isEqualTo(userDto);
    verify(userRepository).save(any(User.class));
    verify(userMapper).toDto(any(User.class));
  }

  @Test
  @DisplayName("createUser() 실패 - 중복 이메일")
  void createUser_duplicateEmail_throwsException() {
    // given
    given(userRepository.existsByEmail(userRegisterRequest.email())).willReturn(true);

    // when & then
    assertThrows(RestException.class, () -> userService.createUser(userRegisterRequest));
  }

  @Test
  @DisplayName("login() 성공")
  void login() {
    // given
    given(userRepository.findByEmail(userLoginRequest.email())).willReturn(Optional.of(user));
    given(userMapper.toDto(any(User.class))).willReturn(userDto);

    // when
    UserDto result = userService.login(userLoginRequest);

    // then
    assertThat(result).isEqualTo(userDto);
    verify(userMapper).toDto(any(User.class));
  }

  @Test
  @DisplayName("updateUser() 성공")
  void updateUser_saveUser_returnUserDto() {
    // given
    given(userRepository.findById(any(Long.class))).willReturn(Optional.of(user));
    given(userMapper.toDto(any(User.class))).willReturn(updatedUserDto);

    // when
    UserDto result = userService.updateUser(1L, userUpdateRequest);

    // then
    assertThat(result).isEqualTo(updatedUserDto);
    verify(userMapper).toDto(any(User.class));
  }

  @Test
  @DisplayName("deleteUser() 성공")
  void deleteUser() {
    // given
    given(userRepository.findById(any(Long.class))).willReturn(Optional.of(deletedUser));

    // when
    userService.deleteUser(1L);

    // then
    verify(deletedUser).setDeleted();
  }

  @Test
  @DisplayName("deleteUserHard() 성공")
  void deleteUserHard() {
    // given
    given(userRepository.existsById(any(Long.class))).willReturn(true);

    // when
    userService.deleteUserHard(1L);

    // then
    verify(userRepository).deleteById(any(Long.class));
  }
}