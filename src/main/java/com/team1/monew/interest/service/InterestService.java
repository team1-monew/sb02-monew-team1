package com.team1.monew.interest.service;

import com.team1.monew.interest.dto.InterestDto;
import com.team1.monew.interest.dto.InterestRegisterRequest;
import com.team1.monew.interest.dto.InterestSearchCondition;
import com.team1.monew.interest.dto.InterestUpdateRequest;
import org.springframework.data.domain.Slice;

public interface InterestService {
  InterestDto create(InterestRegisterRequest interestRegisterRequest);
  InterestDto update(Long id, Long userId, InterestUpdateRequest interestUpdateRequest);
  Slice<InterestDto> findInterestsWithCursor(Long userId, InterestSearchCondition interestSearchCondition);
  void delete(Long id);
}
