package com.team1.monew.subscription.service;

import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.interest.repository.InterestRepository;
import com.team1.monew.subscription.dto.SubscriptionDto;
import com.team1.monew.subscription.entity.Subscription;
import com.team1.monew.subscription.event.SubscriptionCreateEvent;
import com.team1.monew.subscription.event.SubscriptionDeleteEvent;
import com.team1.monew.subscription.mapper.SubscriptionMapper;
import com.team1.monew.subscription.repository.SubscriptionRepository;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

  private final SubscriptionRepository subscriptionRepository;
  private final InterestRepository interestRepository;
  private final UserRepository userRepository;
  private final SubscriptionMapper subscriptionMapper;

  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public SubscriptionDto create(Long interestId, Long userId) {
    interestRepository.findById(interestId)
        .orElseThrow(() -> {
          log.warn("관심사 구독 실패 - 해당 관심사가 존재하지 않음, interestId: {}", interestId);
          return new RestException(ErrorCode.NOT_FOUND,
              Map.of("interestId", interestId, "detail", "interest not found"));
        });
    User user = userRepository.findById(userId)
        .orElseThrow(() -> {
          log.warn("관심사 구독 실패 - 해당 유저가 존재하지 않음, userId: {}", userId);
          return new RestException(ErrorCode.NOT_FOUND,
              Map.of("userId", userId, "detail", "user not found"));
        });
    checkDuplicateSubscription(interestId, userId);

    interestRepository.incrementSubscriberCount(interestId);
    Interest updatedInterest = interestRepository.findById(interestId).orElseThrow();

    Subscription subscription = new Subscription(user, updatedInterest);
    subscriptionRepository.save(subscription);

    SubscriptionDto subscriptionDto = subscriptionMapper.toDto(subscription);

    eventPublisher.publishEvent(SubscriptionCreateEvent.builder()
        .subscriptionDto(subscriptionDto)
        .userId(userId)
        .build());

    log.info("관심사 구독 완료 - interestId: {}, userId: {}", interestId, userId);

    return subscriptionDto;
  }

  @Override
  @Transactional
  public void delete(Long interestId, Long userId) {
    Subscription subscription = subscriptionRepository.findByInterest_IdAndUser_Id(interestId,
            userId)
        .orElseThrow(() -> {
          log.warn("관심사 구독 취소 실패 - 해당 구독 내용이 존재하지 않음, interestId: {}, userId: {}",
              interestId, userId);
          return new RestException(ErrorCode.NOT_FOUND,
              Map.of("interestId", interestId, "userId", userId, "detail",
                  "subscription not found"));
        });
    Long subscriptionId = subscription.getId();
    subscriptionRepository.deleteById(subscriptionId);

    interestRepository.decrementSubscriberCount(interestId);

    eventPublisher.publishEvent(SubscriptionDeleteEvent.builder()
        .subscriptionId(subscriptionId)
        .userId(userId)
        .build());

    log.info("관심사 구독 취소 완료 - interestId: {}, userId: {}", interestId, userId);
  }


  private void checkDuplicateSubscription(Long interestId, Long userId) {
    if (subscriptionRepository.existsByInterest_IdAndUser_Id(interestId, userId)) {
      log.warn("관심사 구독 실패 - 이미 구독한 관심사, interestId: {}, userId: {}", interestId, userId);
      throw new RestException(ErrorCode.CONFLICT,
          Map.of("interestId", interestId, "userId", userId, "detail",
              "subscription already exists"));
    }
  }
}
