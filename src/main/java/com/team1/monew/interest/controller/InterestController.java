package com.team1.monew.interest.controller;

import com.team1.monew.interest.dto.InterestDto;
import com.team1.monew.interest.dto.InterestRegisterRequest;
import com.team1.monew.interest.service.InterestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/interests")
public class InterestController {

  private final InterestService interestService;

  @PostMapping
  public ResponseEntity<InterestDto> create(
      @RequestBody @Valid InterestRegisterRequest interestRegisterRequest,
      @RequestHeader("Monew-Request-User-ID") Long userId) {
    log.info("관심사 생성 요청 - userId: {}, interestName: {}", userId, interestRegisterRequest.name());
    InterestDto interestDto = interestService.create(interestRegisterRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(interestDto);
  }
}
