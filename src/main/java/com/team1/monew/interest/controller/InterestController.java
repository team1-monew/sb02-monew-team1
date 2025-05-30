package com.team1.monew.interest.controller;

import com.team1.monew.interest.dto.InterestDto;
import com.team1.monew.interest.dto.InterestRegisterRequest;
import com.team1.monew.interest.dto.InterestUpdateRequest;
import com.team1.monew.interest.service.InterestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

  @PatchMapping("/{interestId}")
  public ResponseEntity<InterestDto> update(
      @RequestBody @Valid InterestUpdateRequest interestUpdateRequest,
      @PathVariable("interestId") Long interestId,
      @RequestHeader("Monew-Request-User-ID") Long userId) {
    log.info("관심사 수정 요청 - userId: {}, interestId: {}", userId, interestId);
    InterestDto interestDto = interestService.update(interestId, interestUpdateRequest);
    return ResponseEntity.status(HttpStatus.OK).body(interestDto);
  }

  @DeleteMapping("/{interestId}")
  public ResponseEntity<Void> delete(
      @PathVariable("interestId") Long interestId,
      @RequestHeader("Monew-Request-User-ID") Long userId) {
    log.info("관심사 삭제 요청 - userId: {}, interestId: {}", userId, interestId);
    interestService.delete(interestId);
    return ResponseEntity.noContent().build();
  }
}
