package com.team1.monew.subscription.repository;

import com.team1.monew.interest.entity.Interest;
import com.team1.monew.article.entity.ArticleView;
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

  @Query("SELECT s FROM Subscription s LEFT JOIN FETCH s.interest WHERE s.user.id = :userId")
  List<Subscription> findByUserIdFetch(Long userId);

  @Query("SELECT s FROM Subscription s JOIN FETCH s.user WHERE s.interest = :interest")
  List<Subscription> findAllByInterestWithUser(@Param("interest") Interest interest);

  // todo: userId, createdAt 관련 인덱스 필요 - 추후 생성 후 성능 측정
  @Query("SELECT s FROM Subscription s "
      + "LEFT JOIN FETCH s.interest i "
      + "WHERE s.user.id = :userId "
      + "ORDER BY s.createdAt DESC")
  List<Subscription> findByUserIdOrderByCreatedAt(Long userId);
}
