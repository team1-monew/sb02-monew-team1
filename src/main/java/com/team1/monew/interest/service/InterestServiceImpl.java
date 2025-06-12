package com.team1.monew.interest.service;

import com.team1.monew.interest.event.KeywordAddedEvent;
import com.team1.monew.interest.event.KeywordRemovedEvent;
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
import com.team1.monew.subscription.repository.SubscriptionRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class InterestServiceImpl implements InterestService {

  private final InterestRepository interestRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final InterestMapper interestMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public InterestDto create(InterestRegisterRequest interestRegisterRequest) {
    checkSimilarityInterestName(interestRegisterRequest.name());
    Interest interest = new Interest(interestRegisterRequest.name());
    interestRegisterRequest.keywords().forEach(keywordStr -> {
      Keyword keyword = new Keyword(keywordStr);
      interest.addKeyword(keyword);
    });
    interestRepository.save(interest);
    log.info("관심사 생성 완료 - interestId: {}, interestName: {}", interest.getId(), interest.getName());

    KeywordAddedEvents(interest, interestRegisterRequest.keywords());

    return interestMapper.toDto(interest, false);
  }

  @Override
  @Transactional
  public InterestDto update(Long id, Long userId, InterestUpdateRequest interestUpdateRequest) {
    Interest interest = interestRepository.findById(id).orElseThrow(() -> {
      log.warn("관심사 수정 실패 - 해당 관심사가 존재하지 않음, id: {}", id);
      return new RestException(ErrorCode.NOT_FOUND,
              Map.of("id", id, "detail", "interest not found"));
    });

    KeywordUpdateResult changeResult = detectKeywordChangesAndUpdateInterest(interest, interestUpdateRequest.keywords());

    interestRepository.save(interest);
    log.info("관심사 수정 완료 - interestId: {}", interest.getId());

    if (!changeResult.added().isEmpty()) {
      KeywordAddedEvents(interest, changeResult.added());
    }

    if (!changeResult.removed().isEmpty()) {
      KeywordRemovedEvents(interest, changeResult.removed());
    }

    return interestMapper.toDto(interest,
            subscriptionRepository.existsByInterest_IdAndUser_Id(id, userId));
  }


  @Override
  @Transactional
  public Slice<InterestDto> findInterestsWithCursor(
      Long userId, InterestSearchCondition interestSearchCondition) {
    Slice<Interest> interests = interestRepository.searchByCondition(interestSearchCondition);
    // 쿼리를 1번만 날리기 위해 userId를 가진 subscription id 전체 조회
    // contains 연산을 O(1)로 하기 위해 set 사용
    Set<Long> subscribedSet = new HashSet<>(
        subscriptionRepository.findSubscribedInterestIdByUserId(userId));
    return interests.map(
        interest -> interestMapper.toDto(interest, subscribedSet.contains(interest.getId())));
  }

  @Override
  @Transactional
  public void delete(Long id) {
    Interest interest = interestRepository.findById(id).orElseThrow(() -> {
      log.warn("관심사 삭제 실패 - 해당 관심사가 존재하지 않음, id: {}", id);
      return new RestException(ErrorCode.NOT_FOUND,
          Map.of("id", id, "detail", "interest not found"));
    });
    interestRepository.deleteById(id);
    log.info("관심사 삭제 완료 - interestId: {}", interest.getId());
  }


  // 연속된 앞쪽(접두사) + 뒷쪽(접미사) 문자의 유사도가 80%가 넘는지 테스트
  private void checkSimilarityInterestName(String name) {
    List<Interest> interests = interestRepository.findAll();
    List<String> existingNames = interests.stream().map(Interest::getName).toList();

    for (String existing : existingNames) {
      int prefix = countPrefixMatch(name, existing);
      int suffix = countSuffixMatch(name, existing);

      int totalMatch = prefix + suffix;
      int baseLength = Math.max(name.length(), existing.length());

      double similarity = (double) totalMatch / baseLength;

      if (similarity >= 0.8) {
        log.warn("관심사 등록 실패 - 유사도 80% 이상, name: {}, existing: {}", name, existing);
        throw new RestException(ErrorCode.SIMILARITY_OVER_VIOLATION,
            Map.of("interestName", name, "detail", "interest name similarity over 80"));
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

  private void KeywordAddedEvents(Interest interest, Iterable<String> keywords) {
    keywords.forEach(keywordStr -> {
      log.info("키워드 추가 이벤트 발행 - keyword: {}", keywordStr);
      eventPublisher.publishEvent(new KeywordAddedEvent(interest, keywordStr));
    });
  }

  private void KeywordRemovedEvents(Interest interest, Iterable<String> keywords) {
    keywords.forEach(keywordStr -> {
      log.info("키워드 제거 이벤트 발행 - keyword: {}", keywordStr);
      eventPublisher.publishEvent(new KeywordRemovedEvent(interest, keywordStr));
    });
  }

  private KeywordUpdateResult detectKeywordChangesAndUpdateInterest(Interest interest, List<String> newKeywordStrs) {
    List<String> oldKeywordStrs = interest.getKeywords().stream()
            .map(Keyword::getKeyword)
            .toList();

    Set<String> addedStrs = new HashSet<>(newKeywordStrs);
    oldKeywordStrs.forEach(addedStrs::remove);

    Set<String> removedStrs = new HashSet<>(oldKeywordStrs);
    newKeywordStrs.forEach(removedStrs::remove);

    List<Keyword> newKeywords = newKeywordStrs.stream()
            .map(Keyword::new)
            .toList();
    interest.updateKeywords(newKeywords);

    return new KeywordUpdateResult(addedStrs, removedStrs);
  }

  private static record KeywordUpdateResult(Set<String> added, Set<String> removed) {}
}
