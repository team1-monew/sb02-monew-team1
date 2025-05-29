package com.team1.monew.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
    Instant timestamp,
    int status,
    String message,
    Map<String, Object> details,
    String exceptionType,
    String code
) {
  public ErrorResponse(RestException e) {
    this(
        Instant.now(),
        e.getErrorCode().getStatus().value(),
        e.getMessage(),
        e.getDetails(),
        e.getClass().getSimpleName(),
        e.getErrorCode().name()
    );
  }
}
