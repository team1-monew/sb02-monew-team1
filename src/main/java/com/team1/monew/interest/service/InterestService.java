package com.team1.monew.interest.service;

import com.team1.monew.common.dto.CursorPageResponse;
import com.team1.monew.interest.dto.InterestDto;
import com.team1.monew.interest.dto.InterestRegisterRequest;
import com.team1.monew.interest.dto.InterestSearchCondition;
import com.team1.monew.interest.dto.InterestUpdateRequest;

public interface InterestService {
  InterestDto create(InterestRegisterRequest interestRegisterRequest);
  InterestDto update(Long id, InterestUpdateRequest interestUpdateRequest);
  CursorPageResponse<InterestDto> findAll(InterestSearchCondition interestSearchCondition);
  void delete(Long id);
}
