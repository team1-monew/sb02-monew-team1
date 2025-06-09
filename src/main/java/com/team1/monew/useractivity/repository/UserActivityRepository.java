package com.team1.monew.useractivity.repository;

import com.team1.monew.useractivity.entity.UserActivity;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserActivityRepository extends MongoRepository<UserActivity, Long> {
  Optional<UserActivity> findByUser_Id(Long userId);

}
