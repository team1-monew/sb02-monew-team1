package com.team1.monew.interest.service;

import com.team1.monew.common.dto.CursorPageResponse;
import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import com.team1.monew.interest.dto.InterestDto;
import com.team1.monew.interest.dto.InterestRegisterRequest;
import com.team1.monew.interest.dto.InterestSearchCondition;
import com.team1.monew.interest.dto.InterestUpdateRequest;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.interest.entity.Keyword;
import com.team1.monew.interest.mapper.InterestMapper;
import com.team1.monew.interest.repository.InterestRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class InterestServiceImpl implements InterestService {

  private final InterestRepository interestRepository;
  private final InterestMapper interestMapper;

  @Override
  @Transactional
  public InterestDto create(InterestRegisterRequest interestRegisterRequest) {
    checkSimilarityInterestName(interestRegisterRequest.name());
    Interest interest = new Interest(interestRegisterRequest.name());
    interestRegisterRequest.keywords().forEach(keywordStr -> {
      Keyword keyword = new Keyword(keywordStr);
      interest.addKeyword(keyword);
    });
    return interestMapper.toDto(interestRepository.save(interest));
  }

  @Override
  public InterestDto update(Long id, InterestUpdateRequest interestUpdateRequest) {
    return null;
  }

  @Override
  public CursorPageResponse<InterestDto> findAll(InterestSearchCondition interestSearchCondition) {
    return null;
  }

  @Override
  public void delete(Long id) {

  }

  // 연속된 앞쪽(접두사) + 뒷쪽(접미사) 문자의 유사도가 80%가 넘는지 테스트
  private void checkSimilarityInterestName(String name) {
    List<Interest> interests = interestRepository.findAll();
    List<String> existingNames = interests.stream()
        .map(Interest::getName)
        .toList();

    for (String existing : existingNames) {
      int prefix = countPrefixMatch(name, existing);
      int suffix = countSuffixMatch(name, existing);

      int totalMatch = prefix + suffix;
      int baseLength = Math.max(name.length(), existing.length());

      double similarity = (double) totalMatch / baseLength;

      if (similarity >= 0.8) {
        log.warn("관심사 등록 실패 - 유사도 80% 이상, name: {} / existing: {}", name, existing);
        throw new RestException(ErrorCode.SIMILARITY_OVER_VIOLATION, Map.of("interestName", name));
      }
    }
  }

  private int countPrefixMatch(String a, String b) {
    int len = Math.min(a.length(), b.length());
    int count = 0;
    for (int i = 0; i < len; i++) {
      if (a.charAt(i) == b.charAt(i)) {
        count++;
      } else {
        break;
      }
    }
    return count;
  }

  private int countSuffixMatch(String a, String b) {
    int len = Math.min(a.length(), b.length());
    int count = 0;
    for (int i = 1; i <= len; i++) {
      if (a.charAt(a.length() - i) == b.charAt(b.length() - i)) {
        count++;
      } else {
        break;
      }
    }
    return count;
  }
}
