package com.team1.monew.useractivity.repository;

import com.team1.monew.useractivity.document.SubscriptionActivity;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface SubscriptionActivityRepository extends MongoRepository<SubscriptionActivity, Long> {
}
