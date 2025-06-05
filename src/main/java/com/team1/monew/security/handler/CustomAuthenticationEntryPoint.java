package com.team1.monew.security.handler;

import com.team1.monew.exception.ErrorCode;
import com.team1.monew.security.support.ErrorResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final ErrorResponseWriter errorResponseWriter;

  @Override
  public void commence(HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException) throws IOException {
    errorResponseWriter.write(response, ErrorCode.UNAUTHORIZED, Map.of("message", "로그인이 필요합니다."));
  }
}
