package com.team1.monew.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.monew.user.dto.UserRegisterRequest;
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

  @Test
  @DisplayName("회원 가입 성공")
  void create() throws Exception {
    // given
    UserRegisterRequest request = UserRegisterRequest.builder()
        .email("user1@email.com")
        .nickname("user1")
        .password("user1@@@")
        .build();

    // when & then
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.email").value("user1@email.com"))
        .andExpect(jsonPath("$.nickname").value("user1"))
        .andExpect(jsonPath("$.createdAt").exists());
  }

  @Test
  @DisplayName("중복 이메일로 회원가입 실패")
  void createUser_duplicateEmail() throws Exception {
    // given: 첫 번째 사용자 생성
    UserRegisterRequest request1 = new UserRegisterRequest(
        "duplicate@example.com",
        "user1",
        "password1"
    );
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request1)))
        .andExpect(status().isCreated());

    // when: 같은 이메일로 재시도
    UserRegisterRequest request2 = new UserRegisterRequest(
        "duplicate@example.com",
        "user2",
        "password2"
    );

    // then: 409 CONFLICT 기대
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request2)))
        .andExpect(status().isConflict());
  }
}