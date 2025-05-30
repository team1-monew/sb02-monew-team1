package com.team1.monew.interest.service;

import com.team1.monew.exception.RestException;
import com.team1.monew.interest.dto.InterestDto;
import com.team1.monew.interest.dto.InterestRegisterRequest;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.interest.mapper.InterestMapper;
import com.team1.monew.interest.repository.InterestRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class InterestServiceTest {

  @Mock
  InterestRepository interestRepository;

  // 공유 의존성이 아니므로 Mocking 하지 않고 실제 객체 사용
  @Spy
  InterestMapper interestMapper;

  @InjectMocks
  InterestServiceImpl interestService;

  @Test
  @DisplayName("관심사 생성 성공")
  void createInterest_success() {
    // given
    InterestRegisterRequest interestRegisterRequest = new InterestRegisterRequest("테스트용관심사",
        List.of("연애", "행복", "여행"));

    given(interestRepository.findAll()).willReturn(List.of());

    ArgumentCaptor<Interest> captor = ArgumentCaptor.forClass(Interest.class);
    given(interestRepository.save(captor.capture())).willAnswer(invocation -> captor.getValue());

    // when
    InterestDto interestDto = interestService.create(interestRegisterRequest);

    // then
    assertThat(interestDto.name()).isEqualTo(captor.getValue().getName());
    assertThat(interestDto.keywords())
        .containsExactlyInAnyOrderElementsOf(interestRegisterRequest.keywords());
    then(interestRepository).should(times(1)).findAll();
    then(interestRepository).should(times(1)).save(any(Interest.class));
  }

  @Test
  @DisplayName("유사도 80% 이상일 때 관심사 생성 실패")
  void createInterest_with_similarityExceeds80Percent_failed() {
    // given
    List<Interest> foundInterests = List.of(new Interest("여행계획서"));
    InterestRegisterRequest interestRegisterRequest = new InterestRegisterRequest("여행계획안",
        List.of("연애", "행복", "여행"));
    given(interestRepository.findAll()).willReturn(foundInterests);

    // when + then
    assertThatThrownBy(() -> interestService.create(interestRegisterRequest))
        .isInstanceOf(RestException.class)
        .hasMessageContaining("유사한 이름이 이미 존재");

    then(interestRepository).should(never()).save(any(Interest.class));
  }

  @Test
  @DisplayName("유사도 경계값(75%)일 때 관심사 생성 성공")
  void createInterest_with_similarity75Percent_success() {
    // given
    List<Interest> foundInterests = List.of(new Interest("인공지능"));
    InterestRegisterRequest interestRegisterRequest = new InterestRegisterRequest("인공지식",
        List.of("연애", "행복", "여행"));
    Interest savedInterest = new Interest(interestRegisterRequest.name());
    given(interestRepository.findAll()).willReturn(foundInterests);
    given(interestRepository.save(any(Interest.class))).willReturn(savedInterest);

    // when
    InterestDto interestDto = interestService.create(interestRegisterRequest);

    // then
    then(interestRepository).should(times(1)).findAll();
    then(interestRepository).should(times(1)).save(any(Interest.class));
  }


}
