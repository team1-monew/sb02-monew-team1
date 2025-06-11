package com.team1.monew.useractivity.repository;

import com.team1.monew.useractivity.document.SubscriptionActivity;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface SubscriptionActivityRepository extends MongoRepository<SubscriptionActivity, Long> {

    @Query(value = "{ '_id': ?0 }", fields = "{ 'subscriptions': { '$slice': 10 } }")
    Optional<SubscriptionActivity> findTop10SubscriptionsByUserId(Long userId);

}
