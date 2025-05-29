package com.team1.monew.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
    Instant timestamp,
    int status,
    String message,
    Map<String, Object> details
) {
  public ErrorResponse(Code code, Map<String, Object> details) {
    this(Instant.now(), code.getStatus().value(), code.getMessage(), details);
  }
}
