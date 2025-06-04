package com.team1.monew.security;

import com.team1.monew.user.dto.UserRegisterRequest;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import com.team1.monew.user.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserRepository userRepository;

  private UserRegisterRequest userRegisterRequest;
  private User user;
  @Autowired
  private UserServiceImpl userServiceImpl;

  @BeforeEach
  void setUp() {
    userRegisterRequest = UserRegisterRequest.builder()
        .email("user1@email.com")
        .nickname("user1")
        .password("user1@@@")
        .build();

    user = User.builder()
        .email(userRegisterRequest.email())
        .nickname(userRegisterRequest.nickname())
        .password(userRegisterRequest.password())
        .build();

    ReflectionTestUtils.setField(user, "id", 1L);

    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("인증 없이 허용된 경로는 접근 가능")
  void publicEndpointAccessible() throws Exception {
    mockMvc.perform(get("/"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("헤더 없으면 보호된 경로는 401 반환")
  void protectedEndpoint_withoutHeader_returnsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/protected"))
        .andExpect(status().isUnauthorized());
  }
}
