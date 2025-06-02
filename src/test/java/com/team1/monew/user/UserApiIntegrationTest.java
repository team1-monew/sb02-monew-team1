package com.team1.monew.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.monew.user.dto.UserLoginRequest;
import com.team1.monew.user.dto.UserRegisterRequest;
import com.team1.monew.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class UserApiIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private UserService userService;

  @Test
  @DisplayName("회원 가입 성공")
  void create_success() throws Exception {
    // given
    UserRegisterRequest userRegisterRequest = UserRegisterRequest.builder()
        .email("register_success@email.com")
        .nickname("nickname")
        .password("password")
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
  @DisplayName("중복 이메일로 회원가입 실패")
  void createUser_duplicate_email() throws Exception {
    // given: 첫 번째 사용자 생성
    UserRegisterRequest userRegisterRequest1 = new UserRegisterRequest(
        "register_duplicate_email@example.com",
        "nickname",
        "password"
    );
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userRegisterRequest1)))
        .andExpect(status().isCreated());

    // when: 같은 이메일로 재시도
    UserRegisterRequest userRegisterRequest2 = new UserRegisterRequest(
        "register_duplicate_email@example.com",
        "nickname",
        "password"
    );

    // then: 409 CONFLICT 기대
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userRegisterRequest2)))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("로그인 성공")
  void login_success() throws Exception {
    // given
    // 사용자 생성
    UserRegisterRequest userRegisterRequest = UserRegisterRequest.builder()
        .email("login_success@email.com")
        .nickname("nickname")
        .password("password")
        .build();

    userService.createUser(userRegisterRequest);

    // 로그인 요청
    UserLoginRequest userLoginRequest = UserLoginRequest.builder()
        .email("login_success@email.com")
        .password("password")
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
  @DisplayName("로그인 실패 - 존재하지 않는 이메일")
  void login_email_not_found() throws Exception {
    // given
    // 사용자 생성
    UserRegisterRequest userRegisterRequest = UserRegisterRequest.builder()
        .email("login_email_not_found1@email.com")
        .nickname("nickname")
        .password("password")
        .build();

    userService.createUser(userRegisterRequest);

    // 존재하지 않는 이메일로 로그인 요청
    UserLoginRequest userLoginRequest = UserLoginRequest.builder()
        .email("doesnt_exist_email@email.com")
        .password("password")
        .build();

    // when & then
    // then: 404 NOT_FOUND 기대
    mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userLoginRequest)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("로그인 실패 - 비밀번호 불일치")
  void login_incorrect_password() throws Exception {
    // given
    // 사용자 생성
    UserRegisterRequest userRegisterRequest = UserRegisterRequest.builder()
        .email("login_incorrect_password@email.com")
        .nickname("nickname")
        .password("password")
        .build();

    userService.createUser(userRegisterRequest);

    // 잘못된 비밀번호로 로그인 요청
    UserLoginRequest userLoginRequest = UserLoginRequest.builder()
        .email("login_incorrect_password@email.com")
        .password("password2")
        .build();

    // when & then
    // then: 404 NOT_FOUND 기대
    mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userLoginRequest)))
        .andExpect(status().isNotFound());
  }
}