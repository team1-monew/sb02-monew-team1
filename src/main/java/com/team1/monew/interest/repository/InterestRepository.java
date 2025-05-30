package com.team1.monew.interest.repository;

import com.team1.monew.interest.entity.Interest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InterestRepository extends JpaRepository<Interest, Long> {

  @Query("SELECT i FROM Interest i LEFT JOIN FETCH i.keywords WHERE i.id = :id")
  Optional<Interest> findByIdFetch(@Param("id") Long id);

}
