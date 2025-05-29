package com.team1.monew.exception;

import java.time.Instant;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e) {
    log.error("예상치 못한 서버 내부 오류 처리: {} ", e.getMessage(), e);
    ErrorResponse errorResponse = new ErrorResponse(
        Instant.now(),
        ErrorCode.INTERNAL_SERVER_ERROR.getStatus().value(),
        "예상치 못한 서버 내부 오류가 발생했습니다.",
        Map.of(),
        e.getClass().getSimpleName(),
        ErrorCode.INTERNAL_SERVER_ERROR.getStatus().name()
    );
    return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus()).body(errorResponse);
  }

  @ExceptionHandler(RestException.class)
  public ResponseEntity<ErrorResponse> handleRestException(RestException e) {
    log.warn("RestException 예외 처리 - errorCode: {}", e.getErrorCode().name());
    return ResponseEntity.status(e.getErrorCode().getStatus()).body(new ErrorResponse(e));
  }
}
