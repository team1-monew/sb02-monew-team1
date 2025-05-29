package com.team1.monew.interest.service;

import com.team1.monew.interest.dto.SubscriptionDto;
import java.util.UUID;

public interface SubscriptionService {
  SubscriptionDto create(Long interestId, Long userId);
  void delete(Long interestId, Long userId);
}
