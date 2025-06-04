package com.team1.monew.subscription.service;

import com.team1.monew.subscription.dto.SubscriptionDto;

public interface SubscriptionService {
  SubscriptionDto create(Long interestId, Long userId);
  void delete(Long interestId, Long userId);
}
