package com.team1.monew.interest.service;

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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class InterestServiceTest {

  @Mock
  InterestRepository interestRepository;

  @Mock
  SubscriptionRepository subscriptionRepository;

  @Mock
  ApplicationEventPublisher eventPublisher;

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

  @Test
  @DisplayName("관심사 수정 성공")
  void updateInterest_success() {
    // given
    Interest interest = new Interest("테스트용");
    ReflectionTestUtils.setField(interest, "id",1L);
    interest.addKeyword(new Keyword("테스트"));
    interest.addKeyword(new Keyword("키워드"));
    InterestUpdateRequest interestUpdateRequest = new InterestUpdateRequest(List.of("수정", "테스트"));

    given(interestRepository.findById(any(Long.class))).willReturn(Optional.of(interest));
    given(interestRepository.save(any(Interest.class))).willReturn(interest);
    given(subscriptionRepository.existsByInterest_IdAndUser_Id(any(Long.class), any(Long.class))).willReturn(true);

    // when
    InterestDto interestDto = interestService.update(1L,1L, interestUpdateRequest);

    // then
    assertThat(interestDto.keywords())
        .containsExactlyInAnyOrderElementsOf(interestUpdateRequest.keywords());
    then(interestRepository).should(times(1)).findById(any(Long.class));
    then(interestRepository).should(times(1)).save(any(Interest.class));
  }

  @Test
  @DisplayName("관심사가 존재하지 않을 때, 관심사 수정 실패")
  void updateInterest_notFoundInterest_failed() {
    // given
    InterestUpdateRequest interestUpdateRequest = new InterestUpdateRequest(List.of("수정", "테스트"));
    given(interestRepository.findById(any(Long.class))).willReturn(Optional.empty());

    // when + then
    assertThatThrownBy(() -> interestService.update(1L,1L, interestUpdateRequest))
        .isInstanceOf(RestException.class)
        .hasMessageContaining("찾을 수 없습니다.");
    then(interestRepository).should(never()).save(any(Interest.class));
  }

  @Test
  @DisplayName("관심사 삭제 성공")
  void deleteInterest_success() {
    // given
    Interest interest = new Interest("테스트용");
    ReflectionTestUtils.setField(interest, "id",1L);
    given(interestRepository.findById(any(Long.class))).willReturn(Optional.of(interest));

    // when
    interestService.delete(1L);

    // then
    then(interestRepository).should(times(1)).deleteById(any(Long.class));
  }

  @Test
  @DisplayName("관심사가 존재하지 않을 때, 관심사 삭제 실패")
  void deleteInterest_notFoundInterest_failed() {
    // given
    given(interestRepository.findById(any(Long.class))).willReturn(Optional.empty());

    // when + then
    assertThatThrownBy(() -> interestService.delete(1L))
        .isInstanceOf(RestException.class)
        .hasMessageContaining("찾을 수 없습니다.");

    then(interestRepository).should(never()).deleteById(any(Long.class));
  }

  @Test
  @DisplayName("관심사 조회 성공")
  void findInterests_success() {
    // given
    Interest interest = new Interest("테스트");
    ReflectionTestUtils.setField(interest, "id", 1L);
    Slice<Interest> interests = new SliceImpl<>(List.of(interest));
    given(interestRepository.searchByCondition(any())).willReturn(interests);
    given(subscriptionRepository.findSubscribedInterestIdByUserId(any(Long.class))).willReturn(List.of(interest.getId()));

    // when
    Slice<InterestDto> interestDtoList = interestService.findInterestsWithCursor(1L, mock(InterestSearchCondition.class));

    // then
    assertThat(interestDtoList.getSize()).isEqualTo(1);
    assertThat(interestDtoList.getContent().get(0).name()).isEqualTo("테스트");
    assertThat(interestDtoList.getContent().get(0).subscribedByMe()).isTrue();
    then(interestRepository).should(times(1)).searchByCondition(any());
  }
}
