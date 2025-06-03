package com.team1.monew.interest.repository;

import com.team1.monew.interest.dto.InterestSearchCondition;
import com.team1.monew.interest.entity.Interest;
import org.springframework.data.domain.Slice;

public interface InterestRepositoryCustom {
  Slice<Interest> searchByCondition(InterestSearchCondition condition);
}
