package com.team1.monew.security.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.ErrorResponse;
import com.team1.monew.exception.RestException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ErrorResponseWriter {
  private final ObjectMapper objectMapper;

  @Autowired // 생략해도 됩니다. (생성자가 1개일 때는 자동 인식됨)
  public ErrorResponseWriter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public void write(HttpServletResponse response, ErrorCode errorCode, Map<String, Object> details) throws IOException {
    ErrorResponse errorResponse = new ErrorResponse(
        LocalDateTime.now(),
        errorCode.getStatus().value(),
        errorCode.getMessage(),
        details,
        errorCode.getClass().getSimpleName(),
        errorCode.name()
    );

    response.setStatus(errorCode.getStatus().value());
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }

  public void write(HttpServletResponse response, RestException e) throws IOException {
    write(response, e.getErrorCode(), e.getDetails());
  }
}