package com.team1.monew.subscription.repository;

import com.team1.monew.subscription.entity.Subscription;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

  Optional<Subscription> findByInterest_IdAndUser_Id(Long interestId, Long UserId);
  boolean existsByInterest_IdAndUser_Id(Long interestId, Long UserId);

}
