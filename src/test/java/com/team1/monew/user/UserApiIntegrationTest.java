package com.team1.monew.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.monew.user.dto.UserDto;
import com.team1.monew.user.dto.UserLoginRequest;
import com.team1.monew.user.dto.UserRegisterRequest;
import com.team1.monew.user.dto.UserUpdateRequest;
import com.team1.monew.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class UserApiIntegrationTest {
  @Container
  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

  @DynamicPropertySource
  static void setMongoUri(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
  }

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private UserService userService;

  @Autowired
  private MongoTemplate mongoTemplate;

  @BeforeEach
  void cleanUp() {
    mongoTemplate.getDb().drop();
  }

  @Test
  @DisplayName("회원 가입 API 통합테스트")
  void create_success() throws Exception {
    // given
    UserRegisterRequest userRegisterRequest = UserRegisterRequest.builder()
        .email("register_success@email.com")
        .nickname("nickname")
        .password("password123@@@")
        .build();

    // when & then
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userRegisterRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.email").value("register_success@email.com"))
        .andExpect(jsonPath("$.nickname").value("nickname"))
        .andExpect(jsonPath("$.createdAt").exists());
  }

  @Test
  @DisplayName("회원가입 API 통합테스트 - 유효하지 않은 입력값으로 실패")
  void createUser_invalid_input() throws Exception {
    // given: 유효하지 않은 이메일과 너무 짧은 비밀번호, 공백 닉네임
    UserRegisterRequest invalidRequest = new UserRegisterRequest(
        "invalid-email",     // 이메일 형식 아님
        "",                  // 닉네임 공백
        "123"                // 비밀번호 길이 부족
    );

    // when & then
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("입력값이 유효하지 않습니다."))
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.details.email").exists())
        .andExpect(jsonPath("$.details.nickname").exists())
        .andExpect(jsonPath("$.details.password").exists());
  }

  @Test
  @DisplayName("회원 가입 API 통합테스트 - 중복 이메일로 회원가입 실패")
  void createUser_duplicate_email() throws Exception {
    // given: 첫 번째 사용자 생성
    UserRegisterRequest userRegisterRequest1 = new UserRegisterRequest(
        "register_duplicate_email@example.com",
        "nickname",
        "password123@@@"
    );
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userRegisterRequest1)))
        .andExpect(status().isCreated());

    // when: 같은 이메일로 재시도
    UserRegisterRequest userRegisterRequest2 = new UserRegisterRequest(
        "register_duplicate_email@example.com",
        "nickname",
        "password123@@@"
    );

    // then: 409 CONFLICT 기대
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userRegisterRequest2)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("CONFLICT"))
        .andExpect(jsonPath("$.message").value("이미 존재하는 리소스입니다."));
  }

  @Test
  @DisplayName("로그인 API 통합테스트")
  void login_success() throws Exception {
    // given
    // 사용자 생성
    UserRegisterRequest userRegisterRequest = UserRegisterRequest.builder()
        .email("login_success@email.com")
        .nickname("nickname")
        .password("password123@@@")
        .build();

    userService.createUser(userRegisterRequest);

    // 로그인 요청
    UserLoginRequest userLoginRequest = UserLoginRequest.builder()
        .email("login_success@email.com")
        .password("password123@@@")
        .build();

    // when & then
    mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userLoginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.email").value("login_success@email.com"))
        .andExpect(jsonPath("$.nickname").value("nickname"))
        .andExpect(jsonPath("$.createdAt").exists());
  }

  @Test
  @DisplayName("로그인 API 통합테스트 - 존재하지 않는 이메일")
  void login_email_not_found() throws Exception {
    // given
    // 사용자 생성
    UserRegisterRequest userRegisterRequest = UserRegisterRequest.builder()
        .email("login_email_not_found1@email.com")
        .nickname("nickname")
        .password("password123@@@")
        .build();

    userService.createUser(userRegisterRequest);

    // 존재하지 않는 이메일로 로그인 요청
    UserLoginRequest userLoginRequest = UserLoginRequest.builder()
        .email("doesnt_exist_email@email.com")
        .password("password123@@@")
        .build();

    // when & then
    // then: 401 UNAUTHORIZED 기대
    mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userLoginRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
        .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."));
  }

  @Test
  @DisplayName("로그인 API 통합테스트 - 비밀번호 불일치")
  void login_incorrect_password() throws Exception {
    // given
    // 사용자 생성
    UserRegisterRequest userRegisterRequest = UserRegisterRequest.builder()
        .email("login_incorrect_password@email.com")
        .nickname("nickname")
        .password("password123@@@")
        .build();

    userService.createUser(userRegisterRequest);

    // 잘못된 비밀번호로 로그인 요청
    UserLoginRequest userLoginRequest = UserLoginRequest.builder()
        .email("login_incorrect_password@email.com")
        .password("password2123@@@")
        .build();

    // when & then
    // then: 401 UNAUTHORIZED 기대
    mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userLoginRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
        .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."));
  }

  @Test
  @DisplayName("사용자 수정 API 통합테스트")
  void updateUser_success() throws Exception {
    // given
    // 사용자 생성
    UserRegisterRequest userRegisterRequest = UserRegisterRequest.builder()
        .email("updateUser_success@email.com")
        .nickname("nickname")
        .password("password123@@@")
        .build();

    UserDto createdUser = userService.createUser(userRegisterRequest);
    Long userId = createdUser.id();


    // 업데이트 Dto 생성
    UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
        .nickname("newNickname")
        .build();

    // when & then
    mockMvc.perform(patch("/api/users/{userId}", userId)
            .header("Monew-Request-User-ID", userId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userUpdateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.email").value("updateuser_success@email.com"))
        .andExpect(jsonPath("$.nickname").value("newNickname"))
        .andExpect(jsonPath("$.createdAt").exists());
  }

  @Test
  @DisplayName("사용자 논리 삭제 API 통합테스트")
  void deleteUser_success() throws Exception {
    // given
    // 사용자 생성
    UserRegisterRequest userRegisterRequest = UserRegisterRequest.builder()
        .email("deleteUser_success@email.com")
        .nickname("nickname")
        .password("password123@@@")
        .build();

    UserDto createdUser = userService.createUser(userRegisterRequest);
    Long userId = createdUser.id();

    // when & then
    mockMvc.perform(delete("/api/users/{userId}", userId)
            .header("Monew-Request-User-ID", userId.toString()))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("사용자 물리 삭제 API 통합테스트")
  void deleteUserHard_success() throws Exception {
    // given
    // 사용자 생성
    UserRegisterRequest userRegisterRequest = UserRegisterRequest.builder()
        .email("deleteUserHard_success@email.com")
        .nickname("nickname")
        .password("password123@@@")
        .build();

    UserDto createdUser = userService.createUser(userRegisterRequest);
    Long userId = createdUser.id();

    // when & then
    mockMvc.perform(delete("/api/users/{userId}/hard", userId)
            .header("Monew-Request-User-ID", userId.toString()))
        .andExpect(status().isNoContent());
  }
}