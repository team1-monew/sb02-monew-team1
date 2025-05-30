package com.team1.monew.user.service;

import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import com.team1.monew.user.dto.UserDto;
import com.team1.monew.user.dto.UserRegisterRequest;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.mapper.UserMapper;
import com.team1.monew.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Override
  public UserDto createUser(UserRegisterRequest userRegisterRequest) {
    log.debug("사용자 생성 시작: email={}, nickname={}", userRegisterRequest.email(), userRegisterRequest.nickname());
    validateEmailNotDuplicated(userRegisterRequest.email());

    User user = User.builder()
            .email(userRegisterRequest.email())
                .nickname(userRegisterRequest.nickname())
                    .password(userRegisterRequest.password())
                        .build();

    user = userRepository.save(user);
    log.info("사용자 생성 완료: id={}, email={}, nickname={}", user.getId(), user.getEmail(), user.getNickname());
    return userMapper.toDto(user);
  }

  @Override
  public void validateEmailNotDuplicated(String email) {
    if (userRepository.existsByEmail(email)) {
      throw new RestException(ErrorCode.CONFLICT);
    }
  }
}
