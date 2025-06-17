package com.team1.monew.subscription.controller;


import com.team1.monew.subscription.controller.api.SubscriptionApi;
import com.team1.monew.subscription.dto.SubscriptionDto;
import com.team1.monew.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class SubscriptionController implements SubscriptionApi {

  private final SubscriptionService subscriptionService;

  @PostMapping("/api/interests/{interestId}/subscriptions")
  public ResponseEntity<SubscriptionDto> create(
      @PathVariable("interestId") Long interestId,
      @RequestHeader("Monew-Request-User-ID") Long userId) {
    log.info("관심사 구독 요청 - userId: {}, interestId: {}", userId, interestId);
    SubscriptionDto subscriptionDto =  subscriptionService.create(interestId, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionDto);
  }

  @DeleteMapping("/api/interests/{interestId}/subscriptions")
  public ResponseEntity<Void> delete(
      @PathVariable("interestId") Long interestId,
      @RequestHeader("Monew-Request-User-ID") Long userId) {
    log.info("관심사 구독 취소 - userId: {}, interestId: {}", userId, interestId);
    subscriptionService.delete(interestId, userId);
    return ResponseEntity.noContent().build();
  }
}
