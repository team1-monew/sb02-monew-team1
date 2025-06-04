package com.team1.monew.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team1.monew.user.dto.UserRegisterRequest;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class HeaderUserAuthenticationFilterTest {

  @Mock
  UserRepository userRepository;

  @Mock
  FilterChain filterChain;

  @InjectMocks
  HeaderUserAuthenticationFilter filter;

  private UserRegisterRequest userRegisterRequest;
  private User user;

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
  @DisplayName("유효한 헤더가 있으면 인증 성공")
  void validHeader_setsAuthentication() throws Exception {
    // given
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/protected");
    request.addHeader("Monew-Request-User-ID", "1");
    MockHttpServletResponse response = new MockHttpServletResponse();

    given(userRepository.findById(1L)).willReturn(Optional.of(user));

    // when
    filter.doFilter(request, response, filterChain);

    // then
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("요청 경로가 '/'인 경우 필터 동작하지 않고 통과")
  void rootPath_shouldBypassAuthenticationFilter() throws Exception {
    // given
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
    MockHttpServletResponse response = new MockHttpServletResponse();

    // when
    filter.doFilter(request, response, filterChain);

    // then
    verify(filterChain, times(1)).doFilter(request, response); // 필터 체인 통과
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull(); // 인증 정보 없음
  }

  @Test
  @DisplayName("헤더에 유저 ID가 없으면 401 응답")
  void noHeader_returnsUnauthorized() throws Exception {
    // given
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/protected");
    MockHttpServletResponse response = new MockHttpServletResponse();

    // when
    filter.doFilter(request, response, filterChain);

    // then
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    verify(filterChain, never()).doFilter(any(), any());
  }

  @Test
  @DisplayName("존재하지 않는 유저 ID는 예외 발생")
  void unknownUserId_throwsException() {
    // given
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/protected");
    request.addHeader("Monew-Request-User-ID", "99");
    MockHttpServletResponse response = new MockHttpServletResponse();

    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    // when & then
    assertThrows(RuntimeException.class, () -> filter.doFilter(request, response, filterChain));
  }
}