package com.team1.monew.interest.repository;

import com.team1.monew.interest.entity.Interest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InterestRepository extends JpaRepository<Interest, Long>, InterestRepositoryCustom {

  @Query("SELECT i FROM Interest i LEFT JOIN FETCH i.keywords")
  List<Interest> findAllWithKeywords();
}
