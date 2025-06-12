package com.team1.monew.useractivity.repository;

import com.team1.monew.useractivity.document.ArticleViewActivity;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ArticleViewActivityRepository extends MongoRepository<ArticleViewActivity, Long> {
    @Query(value = "{ '_id': ?0 }", fields = "{ 'articleViews': { '$slice': 10 } }")
    Optional<ArticleViewActivity> findTop10ArticleViewsByUserId(Long userId);

}
