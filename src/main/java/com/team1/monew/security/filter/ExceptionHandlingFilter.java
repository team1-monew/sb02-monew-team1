package com.team1.monew.security.filter;

import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import com.team1.monew.security.support.ErrorResponseWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.web.filter.OncePerRequestFilter;

public class ExceptionHandlingFilter extends OncePerRequestFilter {
  private final ErrorResponseWriter errorResponseWriter;

  public ExceptionHandlingFilter(ErrorResponseWriter errorResponseWriter) {
    this.errorResponseWriter = errorResponseWriter;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    try {
      filterChain.doFilter(request, response);
    } catch (RestException e) {
      errorResponseWriter.write(response, e);
    } catch (Exception e) {
      errorResponseWriter.write(response, ErrorCode.INTERNAL_SERVER_ERROR, Map.of("message", "필터 처리 중 시스템 오류"));
    }
  }
}
