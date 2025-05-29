package com.team1.monew.exception;

import org.springframework.http.HttpStatus;

public interface Code {
  HttpStatus getStatus();
  String getMessage();
}
