package com.team1.monew.interest.repository;

import com.team1.monew.config.QueryDslConfig;
import com.team1.monew.interest.dto.InterestSearchCondition;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.interest.entity.Keyword;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
public class InterestRepositoryCustomImplTest {

  @Autowired
  private InterestRepository interestRepository;

  @Autowired
  private EntityManager entityManager;


  @BeforeEach
    void setUp() {
      Interest interest1 = new Interest("여행");
      interest1.updateSubscriberCount(100L);
      interest1.addKeyword(new Keyword("휴가"));
      ReflectionTestUtils.setField(interest1, "createdAt", LocalDateTime.of(2025, 6, 3, 3, 0));

      Interest interest2 = new Interest("요리");
      interest2.updateSubscriberCount(200L);
      interest2.addKeyword(new Keyword("레시피"));
      ReflectionTestUtils.setField(interest2, "createdAt", LocalDateTime.of(2025, 6, 3, 5, 0));

      Interest interest3 = new Interest("건강");
      interest3.updateSubscriberCount(150L);
      interest3.addKeyword(new Keyword("운동"));
      ReflectionTestUtils.setField(interest3, "createdAt", LocalDateTime.of(2025, 6, 3, 7, 0));

      Interest interest4 = new Interest("요구르트");
      interest4.updateSubscriberCount(120L);
      interest4.addKeyword(new Keyword("레시트"));
      ReflectionTestUtils.setField(interest4, "createdAt", LocalDateTime.of(2025, 6, 3, 9, 0));

      // 동일 구독자 수 일 때, createdAt을 기준으로 가져오는지 보기 위함
      Interest interest5 = new Interest("동일구독자");
      interest5.updateSubscriberCount(200L);
      interest5.addKeyword(new Keyword("테스트"));
      ReflectionTestUtils.setField(interest5, "createdAt", LocalDateTime.of(2025, 6, 3, 11, 0));

      interestRepository.saveAll(List.of(interest1, interest2, interest3, interest4, interest5));
      entityManager.flush();
      entityManager.clear();
  }

  @Test
  @DisplayName("키워드 포함 검색 + subscriberCount DESC 정렬 성공")
  void searchByCondition_withKeyword_DESC_success() {
    // given
    InterestSearchCondition condition = new InterestSearchCondition(
        "레시",
        null,
        null,
        10,
        "subscriberCount",
        "DESC"
    );

    // when
    Slice<Interest> result = interestRepository.searchByCondition(condition);

    // then
    assertThat(result).hasSize(2);
    assertThat(result.getContent().get(0).getName()).isEqualTo("요리");
    assertThat(result.getContent().get(1).getName()).isEqualTo("요구르트");
    assertThat(result.getContent().get(0).getKeywords()).extracting("keyword")
        .containsExactly("레시피");
    assertThat(result.getContent().get(1).getKeywords()).extracting("keyword")
        .containsExactly("레시트");
  }

  @Test
  @DisplayName("이름 포함 검색 + name ASC 정렬 성공")
  void searchByCondition_withName_ASC_success() {
    // given
    InterestSearchCondition condition = new InterestSearchCondition(
        "요",
        null,
        null,
        10,
        "name",
        "ASC"
    );

    // when
    Slice<Interest> result = interestRepository.searchByCondition(condition);

    // then
    assertThat(result).hasSize(2); // "요리"만 매칭
    assertThat(result.getContent().get(0).getName()).isEqualTo("요구르트");
    assertThat(result.getContent().get(1).getName()).isEqualTo("요리");
    assertThat(result.getContent().get(0).getKeywords()).extracting("keyword")
        .containsExactly("레시트");
    assertThat(result.getContent().get(1).getKeywords()).extracting("keyword")
        .containsExactly("레시피");
  }

  @Test
  @DisplayName("subscriberCount 기준 커서 기반 DESC 정렬 페이징 성공")
  void searchByCondition_withSubscriberCountCursor_DESC_success() {
    // given
    InterestSearchCondition condition = new InterestSearchCondition(
        "",
        "200",  // cursor = subscriberCount of "요리"
        LocalDateTime.of(2025, 6, 3, 5, 0), // after (보조 커서)
        10,
        "subscriberCount",
        "DESC"
    );

    // when
    Slice<Interest> result = interestRepository.searchByCondition(condition);

    // then
    List<String> names = result.getContent().stream()
        .map(Interest::getName)
        .toList();

    assertThat(names).containsExactly("동일구독자","건강", "요구르트", "여행"); // subscriberCount 200보다 작은 순서
  }

  @Test
  @DisplayName("name 기준 커서 기반 ASC 정렬 페이징 성공")
  void searchByCondition_withNameCursor_ASC_success() {
    // given
    InterestSearchCondition condition = new InterestSearchCondition(
        "",
        "건강",   // cursor = name of "건강"
        LocalDateTime.of(2025, 6, 3, 7, 0),// after (보조 커서)
        10,
        "name",
        "ASC"
    );

    // when
    Slice<Interest> result = interestRepository.searchByCondition(condition);

    // then
    List<String> names = result.getContent().stream()
        .map(Interest::getName)
        .toList();

    assertThat(names).containsExactly("동일구독자", "여행", "요구르트", "요리"); // '건강'보다 큰 순서
  }

  @Test
  @DisplayName("마지막 페이지가 아닌 경우 hasNext가 true")
  void searchByCondition_not_lastPage_hasNext_true() {
    // given
    InterestSearchCondition condition = new InterestSearchCondition(
        "",
        "건강",
        LocalDateTime.of(2025, 6, 3, 9, 0),
        1,
        "name",
        "ASC"
    );

    // when
    Slice<Interest> result = interestRepository.searchByCondition(condition);

    // then
    assertThat(result.hasNext()).isTrue();
  }

  @Test
  @DisplayName("경계값 - 마지막 페이지인 경우 hasNext가 false")
  void searchByCondition_lastPage_hasNext_false() {
    // given
    InterestSearchCondition condition = new InterestSearchCondition(
        "",
        "요구르트",
        LocalDateTime.of(2025, 6, 3, 9, 0),
        1,
        "name",
        "ASC"
    );

    // when
    Slice<Interest> result = interestRepository.searchByCondition(condition);

    // then
    assertThat(result.hasNext()).isFalse(); // 마지막 페이지
  }

  @Test
  @DisplayName("검색 결과가 없는 경우")
  void searchByCondition_emptyResult() {
    // given
    InterestSearchCondition condition = new InterestSearchCondition(
        "없음", null, null, 10, "name", "ASC");

    // when
    Slice<Interest> result = interestRepository.searchByCondition(condition);

    // then
    assertThat(result).isEmpty();
  }
}
