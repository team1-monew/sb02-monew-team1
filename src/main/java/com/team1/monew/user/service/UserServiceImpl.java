package com.team1.monew.user.service;

import com.team1.monew.comment.service.CommentLikeCountService;
import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import com.team1.monew.interest.repository.InterestRepository;
import com.team1.monew.subscription.entity.Subscription;
import com.team1.monew.subscription.repository.SubscriptionRepository;
import com.team1.monew.user.dto.UserDto;
import com.team1.monew.user.dto.UserLoginRequest;
import com.team1.monew.user.dto.UserRegisterRequest;
import com.team1.monew.user.dto.UserUpdateRequest;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.event.UserCreateEvent;
import com.team1.monew.user.mapper.UserMapper;
import com.team1.monew.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final InterestRepository interestRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final CommentLikeCountService commentLikeCountService;
  private final UserMapper userMapper;
  private final MongoTemplate mongoTemplate;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  public UserDto createUser(UserRegisterRequest userRegisterRequest) {
    log.debug("사용자 생성 시작: email={}, nickname={}", userRegisterRequest.email(), userRegisterRequest.nickname());

    String normalizedEmail = userRegisterRequest.email().toLowerCase();

    validateEmailNotDuplicated(normalizedEmail);

    User user = User.builder()
            .email(normalizedEmail)
                .nickname(userRegisterRequest.nickname())
                    .password(userRegisterRequest.password())
                        .build();

    user = userRepository.save(user);
    log.info("사용자 생성 완료: id={}, email={}, nickname={}", user.getId(), user.getEmail(),
        user.getNickname());

    eventPublisher.publishEvent(new UserCreateEvent(user.getId()));

    return userMapper.toDto(user);
  }

  @Override
  public UserDto login(UserLoginRequest userLoginRequest) {
    log.debug("로그인 시도: email={}", userLoginRequest.email());

    String normalizedEmail = userLoginRequest.email().toLowerCase();
    String password = userLoginRequest.password();

    User user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new RestException(ErrorCode.INVALID_CREDENTIALS)
            );

    if (!user.getPassword().equals(password) || user.isDeleted()) {
      throw new RestException(ErrorCode.INVALID_CREDENTIALS);
    }

    log.info("로그인 성공: email={}", user.getEmail());
    return userMapper.toDto(user);
  }

  @Override
  public UserDto updateUser(Long id, UserUpdateRequest userUpdateRequest) {
    log.debug("사용자 수정 시도: id={}, request={}", id, userUpdateRequest);

    User user = userRepository.findById(id).orElseThrow(
        () -> new RestException(ErrorCode.NOT_FOUND, Map.of("id", id))
    );

    String newNickname = userUpdateRequest.nickname();

    user.update(newNickname);

    log.info("사용자 수정 완료: id={}", id);
    return userMapper.toDto(user);
  }

  @Override
  public void deleteUser(Long id) {
    User user = userRepository.findById(id).orElseThrow(
        () -> new RestException(ErrorCode.NOT_FOUND, Map.of("id", id))
    );
    user.setDeleted();

    List<Subscription> subscriptionList = subscriptionRepository.findByUserIdFetch(id);
    subscriptionList.forEach(
        subscription -> interestRepository.decrementSubscriberCount(subscription.getInterest().getId()));
    subscriptionRepository.deleteAll(subscriptionList);

    log.info("사용자 논리 삭제 완료: id={}", id);

    commentLikeCountService.updateLikeCountByDeletedUser(id);
  }

  @Override
  public void deleteUserHard(Long id) {
    if (!userRepository.existsById(id)) {
      throw new RestException(ErrorCode.NOT_FOUND, Map.of("id", id));
    }

    List<Subscription> subscriptionList = subscriptionRepository.findByUserIdFetch(id);
    subscriptionList.forEach(
        subscription -> interestRepository.decrementSubscriberCount(subscription.getInterest().getId()));

    commentLikeCountService.updateLikeCountByDeletedUser(id);
    userRepository.deleteById(id);
    log.info("사용자 물리 삭제 완료: id={}", id);
  }

  private void validateEmailNotDuplicated(String email) {
    if (userRepository.existsByEmail(email)) {
      throw new RestException(ErrorCode.CONFLICT);
    }
  }
}
