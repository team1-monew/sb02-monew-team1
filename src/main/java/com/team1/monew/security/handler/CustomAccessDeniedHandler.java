package com.team1.monew.security.handler;

import com.team1.monew.exception.ErrorCode;
import com.team1.monew.security.support.ErrorResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
  private ErrorResponseWriter errorResponseWriter;

  @Override
  public void handle(HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException {
    errorResponseWriter.write(response, ErrorCode.ACCESS_DENIED, Map.of("message", "접근 권한이 없습니다."));
  }
}
