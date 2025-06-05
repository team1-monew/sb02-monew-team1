package com.team1.monew.interest.repository;

import com.team1.monew.interest.entity.Interest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InterestRepository extends JpaRepository<Interest, Long>, InterestRepositoryCustom {

  @Query("SELECT i FROM Interest i LEFT JOIN FETCH i.keywords")
  List<Interest> findAllWithKeywords();

  @Modifying(flushAutomatically = true) //update 전에 flush를 실행하여, 최신 DB 상태를 기준으로 실행
  @Query("UPDATE Interest i SET i.subscriberCount = i.subscriberCount + 1 WHERE i.id = :id")
  void incrementSubscriberCount(@Param("id") Long id);

  @Modifying(flushAutomatically = true)
  @Query("UPDATE Interest i SET i.subscriberCount = i.subscriberCount - 1 WHERE i.id = :id")
  void decrementSubscriberCount(@Param("id") Long id);
}
