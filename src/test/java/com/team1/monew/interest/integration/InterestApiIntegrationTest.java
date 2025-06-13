package com.team1.monew.interest.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.monew.article.event.listener.KeywordAddedEventHandler;
import com.team1.monew.article.event.listener.KeywordRemovedEventHandler;
import com.team1.monew.common.support.IntegrationTestSupport;
import com.team1.monew.interest.dto.InterestRegisterRequest;
import com.team1.monew.interest.dto.InterestUpdateRequest;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.interest.entity.Keyword;
import com.team1.monew.interest.repository.InterestRepository;
import com.team1.monew.user.dto.UserDto;
import com.team1.monew.user.dto.UserRegisterRequest;
import com.team1.monew.user.service.UserService;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.BDDMockito.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("postgres-test")
@Transactional
public class InterestApiIntegrationTest extends IntegrationTestSupport {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private InterestRepository interestRepository;

  @Autowired
  private UserService userService;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoSpyBean
  KeywordAddedEventHandler keywordAddedEventHandler;

  @MockitoSpyBean
  KeywordRemovedEventHandler keywordRemovedEventHandler;

  private Long userId;

  @BeforeEach
  void setUp() {
    UserDto userDto = userService.createUser(UserRegisterRequest.builder()
            .email("test@test.com")
            .nickname("test")
            .password("Test1234!")
        .build());
    userId = userDto.id();
  }

  @Test
  @DisplayName("관심사 생성 API 요청 성공")
  void createInterest_success() throws Exception {
    // given
    InterestRegisterRequest interestRegisterRequest = new InterestRegisterRequest("야구",
        List.of("투수", "타자", "한화"));

    // when + then
    mockMvc.perform(post("/api/interests")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(interestRegisterRequest))
            .header("Monew-Request-User-ID", userId))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("야구"))
        .andExpect(jsonPath("$.keywords").value(Matchers.contains("투수","타자","한화")))
        .andExpect(jsonPath("$.subscriberCount").value(0L))
        .andExpect(jsonPath("$.subscribedByMe").value(false))
        .andExpect(jsonPath("$.createdAt").exists());

    then(keywordAddedEventHandler).should(times(3)).handleKeywordAddedEvent(any());
  }

  @Test
  @DisplayName("관심사 생성 API - 유사도 80% 이상일 때 실패")
  void createInterest_similarity_over80_failed() throws Exception {
    // given
    Interest createdEntity = interestRepository.save(new Interest("야구야구야구"));
    createdEntity.addKeyword(new Keyword("투수"));
    InterestRegisterRequest interestRegisterRequest = new InterestRegisterRequest("야구야구야",
        List.of("투수", "타자", "한화"));
    reset(keywordAddedEventHandler);

    // when + then
    mockMvc.perform(post("/api/interests")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(interestRegisterRequest))
            .header("Monew-Request-User-ID", userId))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("80% 이상의 유사한 이름이 이미 존재합니다."))
        .andExpect(jsonPath("$.code").value("SIMILARITY_OVER_VIOLATION"));

    then(keywordAddedEventHandler).should(never()).handleKeywordAddedEvent(any());
  }

  @Test
  @DisplayName("관심사 목록 조회 API 요청 성공 - 커서 없을 때 이름순 DESC 정렬 전체 조회")
  void getInterests_orderBy_name_DESC_success() throws Exception {
    // given
    Interest createdEntity1 = interestRepository.save(new Interest("여행"));
    createdEntity1.addKeyword(new Keyword("호텔"));
    Interest createdEntity2 = interestRepository.save(new Interest("야구"));
    createdEntity2.addKeyword(new Keyword("야구"));
    Interest createdEntity3 = interestRepository.save(new Interest("게임"));
    createdEntity3.addKeyword(new Keyword("게임"));

    // when + then
    mockMvc.perform(get("/api/interests")
            .param("limit", "2")
            .param("orderBy", "name")
            .header("Monew-Request-User-ID", userId))
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].name").value("여행"))
        .andExpect(jsonPath("$.content[1].name").value("야구"))
        .andExpect(jsonPath("$.hasNext").value(true))
        .andExpect(jsonPath("$.nextCursor").value("야구"))
        .andExpect(jsonPath("$.nextAfter").exists());
  }

  @Test
  @DisplayName("관심사 목록 조회 API 요청 성공 - 커서 있을 때 이름순 DESC 정렬 전체 조회")
  void getInterests_with_cursor_orderBy_name_DESC_success() throws Exception {
    // given
    Interest createdEntity1 = interestRepository.save(new Interest("여행"));
    createdEntity1.addKeyword(new Keyword("호텔"));
    Interest createdEntity2 = interestRepository.save(new Interest("야구"));
    createdEntity2.addKeyword(new Keyword("야구"));
    Interest createdEntity3 = interestRepository.save(new Interest("게임"));
    createdEntity3.addKeyword(new Keyword("게임"));

    // when + then
    mockMvc.perform(get("/api/interests")
            .param("limit","50")
            .param("orderBy", "name")
            .param("cursor", "여행")
            .param("after", createdEntity1.getCreatedAt().toString())
            .header("Monew-Request-User-ID", userId))
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].name").value("야구"))
        .andExpect(jsonPath("$.content[1].name").value("게임"))
        .andExpect(jsonPath("$.hasNext").value(false))
        .andExpect(jsonPath("$.nextCursor").value("게임"))
        .andExpect(jsonPath("$.nextAfter").exists());
  }

  @Test
  @DisplayName("관심사 목록 조회 API 요청 성공 - 커서 없을 때 구독자수 ASC 정렬 및 키워드 필터링 조회")
  void getInterests_with_keyword_orderBy_subscriberCount_ASC_success() throws Exception {
    // given
    Interest createdEntity1 = interestRepository.save(new Interest("여행"));
    Interest createdEntity2 = interestRepository.save(new Interest("야구"));
    Interest createdEntity3 = interestRepository.save(new Interest("여정"));

    createdEntity1.updateSubscriberCount(5L);
    createdEntity2.updateSubscriberCount(3L);
    createdEntity3.updateSubscriberCount(1L);

    // when + then
    mockMvc.perform(get("/api/interests")
            .param("limit", "50")
            .param("orderBy", "subscriberCount")
            .param("direction", "ASC")
            .param("keyword", "여")
            .header("Monew-Request-User-ID", userId))
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].name").value("여정"))
        .andExpect(jsonPath("$.content[1].name").value("여행"))
        .andExpect(jsonPath("$.hasNext").value(false))
        .andExpect(jsonPath("$.nextCursor").value("5"))
        .andExpect(jsonPath("$.nextAfter").exists());
  }

  @Test
  @DisplayName("관심사 목록 조회 API 요청 성공 - 커서 있을 때 구독자수 ASC 정렬 및 키워드 필터링 조회")
  void getInterests_with_cursorAndKeyword_orderBy_subscriberCount_ASC_success() throws Exception {
    // given
    Interest createdEntity1 = interestRepository.save(new Interest("여행"));
    Interest createdEntity2 = interestRepository.save(new Interest("야구"));
    Interest createdEntity3 = interestRepository.save(new Interest("여정"));

    createdEntity1.updateSubscriberCount(5L);
    createdEntity2.updateSubscriberCount(3L);
    createdEntity3.updateSubscriberCount(1L);

    // when + then
    mockMvc.perform(get("/api/interests")
            .param("limit", "50")
            .param("orderBy", "subscriberCount")
            .param("direction", "ASC")
            .param("keyword", "여")
            .param("cursor", "1")
            .param("after", createdEntity3.getCreatedAt().toString())
            .header("Monew-Request-User-ID", userId))
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].name").value("여행"))
        .andExpect(jsonPath("$.hasNext").value(false))
        .andExpect(jsonPath("$.nextCursor").value("5"))
        .andExpect(jsonPath("$.nextAfter").exists());
  }

  @Test
  @DisplayName("관심사 수정 API 요청 성공 - 키워드 추가")
  void updateInterest_addKeyword_success() throws Exception {
    // given
    Interest createdInterest = interestRepository.save(new Interest("여행"));
    InterestUpdateRequest interestUpdateRequest = new InterestUpdateRequest(List.of("해외", "국내"));

    // when + then
    mockMvc.perform(patch("/api/interests/{interestId}", createdInterest.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(interestUpdateRequest))
            .header("Monew-Request-User-ID", userId))
        .andExpect(jsonPath("$.keywords").value(Matchers.contains("해외", "국내")));

    then(keywordAddedEventHandler).should(times(2)).handleKeywordAddedEvent(any());
  }

  @Test
  @DisplayName("관심사 수정 API 요청 성공 - 키워드 삭제")
  void updateInterest_deleteKeyword_success() throws Exception{
    // given
    Interest createdInterest = interestRepository.save(new Interest("여행"));
    createdInterest.addKeyword(new Keyword("해외"));
    createdInterest.addKeyword(new Keyword("국내"));
    InterestUpdateRequest interestUpdateRequest = new InterestUpdateRequest(List.of("해외"));

    // when + then
    mockMvc.perform(patch("/api/interests/{interestId}", createdInterest.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(interestUpdateRequest))
            .header("Monew-Request-User-ID", userId))
        .andExpect(jsonPath("$.keywords").value(Matchers.contains("해외")));

    then(keywordRemovedEventHandler).should(times(1)).handleKeywordRemovedEvent(any());
  }

  @Test
  @DisplayName("관심사 수정 API 요청 - 관심사를 찾을 수 없을 때 실패")
  void updateInterest_notFoundInterest_failed() throws Exception{
    // given
    InterestUpdateRequest interestUpdateRequest = new InterestUpdateRequest(List.of("해외"));

    // when + then
    mockMvc.perform(patch("/api/interests/{interestId}", "1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(interestUpdateRequest))
            .header("Monew-Request-User-ID", userId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("요청한 리소스를 찾을 수 없습니다."))
        .andExpect(jsonPath("$.code").value("NOT_FOUND"));
  }

  @Test
  @DisplayName("관심사 삭제 API 요청 성공")
  void deleteInterest_success() throws Exception{
    // given
    Interest createdInterest = interestRepository.save(new Interest("여행"));

    // when + then
    mockMvc.perform(delete("/api/interests/{interestId}", createdInterest.getId())
            .header("Monew-Request-User-ID", userId))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("관심사 삭제 API 요청 - 관심사를 찾을 수 없을 때 실패")
  void deleteInterest_notFoundInterest_failed() throws Exception{
    // given

    // when + then
    mockMvc.perform(delete("/api/interests/{interestId}", "1")
            .header("Monew-Request-User-ID", userId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("요청한 리소스를 찾을 수 없습니다."))
        .andExpect(jsonPath("$.code").value("NOT_FOUND"));
  }
}
