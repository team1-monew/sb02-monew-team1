package com.team1.monew.security.filter;

import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import com.team1.monew.security.auth.CustomUserDetails;
import com.team1.monew.security.util.SecurityPathUtil;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class HeaderUserAuthenticationFilter extends OncePerRequestFilter {

  private final UserRepository userRepository;

  public HeaderUserAuthenticationFilter(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    String path = request.getRequestURI();

    if (SecurityPathUtil.shouldExclude(path)) {
      filterChain.doFilter(request, response);
      return;
    }

    String userIdHeader = request.getHeader("Monew-Request-User-ID");

    log.debug(String.format("User ID: %s", userIdHeader));

    if (userIdHeader == null) {
      log.warn("인증 헤더 없음 - 요청 경로: {}", path);
      throw new RestException(ErrorCode.UNAUTHORIZED, Map.of("header", "Monew-Request-User-ID 없음"));
    }

    try {
      Long userId = Long.parseLong(userIdHeader);
      User user = userRepository.findById(userId)
          .orElseThrow(() -> {
            log.warn("존재하지 않는 사용자 ID: {}", userId);
            return new RestException(ErrorCode.NOT_FOUND, Map.of("id", userId));
          });

      log.info("인증 성공 - 사용자 ID: {}, 이메일: {}", user.getId(), user.getEmail());

      // 현재 구조에서는 필요없음 (추후 @AuthenticationPrincipal 을 사용한다면 필요)
      UserDetails userDetails = new CustomUserDetails(user);
      Authentication authentication = new UsernamePasswordAuthenticationToken(
          userDetails, null, userDetails.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }  catch (NumberFormatException e) {
      log.warn("잘못된 사용자 ID 형식: {}", userIdHeader);
      throw new RestException(ErrorCode.INVALID_INPUT_VALUE, Map.of("detail", "잘못된 사용자 ID 형식"));

    } catch (RestException e) {
      // NOT_FOUND 같은 우리가 명시적으로 던진 예외는 그대로 던짐
      throw e;

    } catch (Exception e) {
      log.error("예상치 못한 예외 발생 - 필터 처리 중 시스템 오류", e);
      throw new RestException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    filterChain.doFilter(request, response);
  }
}
