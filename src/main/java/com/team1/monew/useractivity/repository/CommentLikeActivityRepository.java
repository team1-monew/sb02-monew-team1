package com.team1.monew.useractivity.repository;

import com.team1.monew.useractivity.document.CommentLikeActivity;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface CommentLikeActivityRepository extends MongoRepository<CommentLikeActivity, String> {

    Optional<CommentLikeActivity> findByUserId(Long userId);

    @Query(value = "{ '_id': ?0 }", fields = "{ 'commentLikes': { '$slice': 10 } }")
    Optional<CommentLikeActivity> findTop10CommentLikesByUserId(Long userId);

}
