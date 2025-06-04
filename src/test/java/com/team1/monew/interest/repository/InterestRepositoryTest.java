package com.team1.monew.interest.repository;

import com.team1.monew.config.QueryDslConfig;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.interest.entity.Keyword;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
public class InterestRepositoryTest {

  @Autowired
  InterestRepository interestRepository;

  @Autowired
  EntityManager entityManager;

  @Test
  @DisplayName("모든 관심사 찾기 fetch join 성공")
  void findAll_interests_fetchJoin_success() {
    // given
    PersistenceUnitUtil util = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();

    Interest interest1 = new Interest("여행");
    Interest interest2 = new Interest("요리");
    Keyword keyword1 = new Keyword("휴가");
    Keyword keyword2 = new Keyword("방콕");
    Keyword keyword3 = new Keyword("레시피");
    interest1.addKeyword(keyword1);
    interest1.addKeyword(keyword2);
    interest2.addKeyword(keyword3);

    interestRepository.saveAll(List.of(interest1, interest2));

    entityManager.flush();
    entityManager.clear();

    // when
    List<Interest> interestList = interestRepository.findAllWithKeywords();

    Interest loadedInterest1 = interestList.stream()
        .filter(i -> i.getName().equals("여행"))
        .findFirst()
        .orElseThrow();

    Interest loadedInterest2 = interestList.stream()
        .filter(i -> i.getName().equals("요리"))
        .findFirst()
        .orElseThrow();

    // then
    assertThat(util.isLoaded(loadedInterest1, "keywords")).isTrue();
    assertThat(loadedInterest1.getKeywords()).hasSize(2)
            .extracting("keyword")
                .containsExactlyInAnyOrder("휴가", "방콕");

    assertThat(util.isLoaded(loadedInterest2, "keywords")).isTrue();
    assertThat(loadedInterest2.getKeywords()).hasSize(1)
        .extracting("keyword")
        .containsExactlyInAnyOrder("레시피");

    assertThat(interestList).hasSize(2)
            .extracting("name")
                .containsExactlyInAnyOrder("여행", "요리");
  }
}
