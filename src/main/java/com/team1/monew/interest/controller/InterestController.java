package com.team1.monew.interest.controller;

import com.team1.monew.common.dto.CursorPageResponse;
import com.team1.monew.interest.controller.api.InterestApi;
import com.team1.monew.interest.dto.InterestDto;
import com.team1.monew.interest.dto.InterestRegisterRequest;
import com.team1.monew.interest.dto.InterestSearchCondition;
import com.team1.monew.interest.dto.InterestUpdateRequest;
import com.team1.monew.interest.mapper.InterestPageResponseMapper;
import com.team1.monew.interest.service.InterestService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/interests")
public class InterestController implements InterestApi {

  private final InterestService interestService;
  private final InterestPageResponseMapper pageResponseMapper;

  @PostMapping
  public ResponseEntity<InterestDto> create(
      @RequestBody @Valid InterestRegisterRequest interestRegisterRequest,
      @RequestHeader("Monew-Request-User-ID") Long userId) {
    log.info("관심사 생성 요청 - userId: {}, interestName: {}", userId, interestRegisterRequest.name());
    InterestDto interestDto = interestService.create(interestRegisterRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(interestDto);
  }


  @GetMapping
  public ResponseEntity<CursorPageResponse<InterestDto>> getInterests(
      @RequestParam(name = "direction", defaultValue = "DESC") String direction,
      @RequestParam(name = "limit", defaultValue = "10") int limit,
      @RequestParam(name = "orderBy", defaultValue = "subscriberCount") String orderBy,
      @RequestParam(name = "keyword", required = false) String keyword,
      @RequestParam(name = "cursor", required = false) String cursor,
      @RequestParam(name = "after", required = false) LocalDateTime after,
      @RequestHeader("Monew-Request-User-ID") Long userId) {
    InterestSearchCondition interestSearchCondition = InterestSearchCondition.builder()
        .keyword(keyword)
        .cursor(cursor)
        .direction(direction)
        .limit(limit)
        .orderBy(orderBy)
        .after(after)
        .build();

    Slice<InterestDto> interestDtoList = interestService.findInterestsWithCursor(
        userId, interestSearchCondition);
    return ResponseEntity.status(HttpStatus.OK)
        .body(pageResponseMapper.toPageResponse(interestDtoList, interestSearchCondition));
  }


  @PatchMapping("/{interestId}")
  public ResponseEntity<InterestDto> update(
      @RequestBody @Valid InterestUpdateRequest interestUpdateRequest,
      @PathVariable("interestId") Long interestId,
      @RequestHeader("Monew-Request-User-ID") Long userId) {
    log.info("관심사 수정 요청 - userId: {}, interestId: {}", userId, interestId);
    InterestDto interestDto = interestService.update(interestId, userId, interestUpdateRequest);
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
