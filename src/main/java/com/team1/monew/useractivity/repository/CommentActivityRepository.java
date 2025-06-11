package com.team1.monew.useractivity.repository;

import com.team1.monew.useractivity.document.CommentActivity;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface CommentActivityRepository extends MongoRepository<CommentActivity, String> {

    Optional<CommentActivity> findByUserId(Long userId);

    @Query(value = "{ '_id': ?0 }", fields = "{ 'comments': { '$slice': 10 } }")
    Optional<CommentActivity> findTop10CommentsByUserId(Long userId);

}
