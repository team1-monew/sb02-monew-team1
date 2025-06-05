package com.team1.monew.subscription.service;

import com.team1.monew.exception.RestException;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.interest.repository.InterestRepository;
import com.team1.monew.subscription.dto.SubscriptionDto;
import com.team1.monew.subscription.entity.Subscription;
import com.team1.monew.subscription.mapper.SubscriptionMapper;
import com.team1.monew.subscription.repository.SubscriptionRepository;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceTest {

  @Mock
  SubscriptionRepository subscriptionRepository;

  @Mock
  InterestRepository interestRepository;

  @Mock
  UserRepository userRepository;

  @Spy
  SubscriptionMapper subscriptionMapper;

  @InjectMocks
  SubscriptionServiceImpl subscriptionService;

  @Test
  @DisplayName("관심사 구독 성공")
  void createSubscription_success() {
    // given
    Interest interest = new Interest("테스트");
    ReflectionTestUtils.setField(interest, "id", 1L);
    User user = new User("test@test.com", "testUser", "1234");
    ReflectionTestUtils.setField(user, "id", 1L);
    given(interestRepository.findById(any(Long.class))).willReturn(Optional.of(interest));
    given(userRepository.findById(any(Long.class))).willReturn(Optional.of(user));
    given(subscriptionRepository.existsByInterest_IdAndUser_Id(any(Long.class),
        any(Long.class))).willReturn(false);
    willDoNothing().given(interestRepository).incrementSubscriberCount(1L);

    // when
    SubscriptionDto subscriptionDto = subscriptionService.create(interest.getId(), user.getId());

    // then
    assertThat(subscriptionDto.interestId()).isEqualTo(interest.getId());
    then(subscriptionRepository).should(times(1)).save(any(Subscription.class));
    then(interestRepository).should(times(1)).incrementSubscriberCount(any());
  }

  @Test
  @DisplayName("관심사 구독 실패 - 해당 관심사가 존재하지 않음")
  void createSubscription_interestNotFound_failed() {
    // given
    User user = new User("test@test.com", "testUser", "1234");
    ReflectionTestUtils.setField(user, "id", 1L);
    given(interestRepository.findById(any(Long.class))).willReturn(Optional.empty());

    // when + then
    assertThatThrownBy(() -> subscriptionService.create(1L, user.getId()))
        .isInstanceOf(RestException.class)
        .hasMessageContaining("찾을 수 없습니다.");

    then(subscriptionRepository).should(never()).save(any(Subscription.class));
  }

  @Test
  @DisplayName("관심사 구독 실패 - 해당 유저가 존재하지 않음")
  void createSubscription_userNotFound_failed() {
    // given
    Interest interest = new Interest("테스트");
    ReflectionTestUtils.setField(interest, "id", 1L);
    given(interestRepository.findById(any(Long.class))).willReturn(Optional.of(interest));
    given(userRepository.findById(any(Long.class))).willReturn(Optional.empty());

    // when + then
    assertThatThrownBy(() -> subscriptionService.create(interest.getId(), 1L))
        .isInstanceOf(RestException.class)
        .hasMessageContaining("찾을 수 없습니다.");

    then(subscriptionRepository).should(never()).save(any(Subscription.class));
  }

  @Test
  @DisplayName("관심사 구독 실패 - 이미 구독한 관심사")
  void createSubscription_duplicateSubscription_failed() {
    // given
    Interest interest = new Interest("테스트");
    ReflectionTestUtils.setField(interest, "id", 1L);
    User user = new User("test@test.com", "testUser", "1234");
    ReflectionTestUtils.setField(user, "id", 1L);
    given(interestRepository.findById(any(Long.class))).willReturn(Optional.of(interest));
    given(userRepository.findById(any(Long.class))).willReturn(Optional.of(user));
    given(subscriptionRepository.existsByInterest_IdAndUser_Id(any(Long.class),
        any(Long.class))).willReturn(true);

    // when
    assertThatThrownBy(() -> subscriptionService.create(interest.getId(), user.getId()))
        .isInstanceOf(RestException.class)
        .hasMessageContaining("이미 존재하는 리소스입니다.");

    // then
    then(subscriptionRepository).should(never()).save(any(Subscription.class));
  }

  @Test
  @DisplayName("관심사 구독 취소 성공")
  void deleteSubscription_success() {
    // given
    Interest interest = new Interest("테스트 관심사");
    ReflectionTestUtils.setField(interest, "id", 1L);
    interest.updateSubscriberCount(5L); // 현재 5명 구독 중

    User user = new User("test@test.com", "testUser", "1234");
    ReflectionTestUtils.setField(user, "id", 1L);

    Subscription subscription = new Subscription(user, interest);

    given(subscriptionRepository.findByInterest_IdAndUser_Id(interest.getId(), user.getId()))
        .willReturn(Optional.of(subscription));
    willDoNothing().given(interestRepository).decrementSubscriberCount(1L);

    // when
    subscriptionService.delete(interest.getId(), user.getId());

    // then
    then(subscriptionRepository).should().deleteById(subscription.getId());
    then(interestRepository).should(times(1)).decrementSubscriberCount(any());
  }

  @Test
  @DisplayName("구독 취소 실패 - 구독 정보 없음")
  void deleteSubscription_subscriptionNotFound_failed() {
    // given
    given(subscriptionRepository.findByInterest_IdAndUser_Id(any(Long.class), any(Long.class)))
        .willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> subscriptionService.delete(1L, 1L))
        .isInstanceOf(RestException.class)
        .hasMessageContaining("찾을 수 없습니다.");

    then(subscriptionRepository).should(never()).deleteById(any());
  }
}
