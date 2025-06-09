package com.team1.monew.subscription.repository;

import com.team1.monew.interest.entity.Interest;
import com.team1.monew.subscription.entity.Subscription;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

  Optional<Subscription> findByInterest_IdAndUser_Id(Long interestId, Long UserId);
  boolean existsByInterest_IdAndUser_Id(Long interestId, Long UserId);

  @Query("SELECT s.interest.id FROM Subscription s WHERE s.user.id = :userId")
  List<Long> findSubscribedInterestIdByUserId(Long userId);

  // 활동내역 구독 조회할 때 쓸 것
  @Query("SELECT s FROM Subscription s LEFT JOIN FETCH s.interest WHERE s.user.id = :userId")
  List<Subscription> findByUserIdFetch(Long userId);

  // 알림관리에서 사용
  @Query("SELECT s FROM Subscription s JOIN FETCH s.user WHERE s.interest = :interest")
  List<Subscription> findAllByInterestWithUser(@Param("interest") Interest interest);
}
