package com.team1.monew.notification.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.team1.monew.article.dto.CollectedArticleDto;
import com.team1.monew.article.event.NewArticlesCollectedEvent;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.interest.repository.InterestRepository;
import com.team1.monew.notification.dto.ResourceType;
import com.team1.monew.notification.entity.Notification;
import com.team1.monew.notification.repository.NotificationRepository;
import com.team1.monew.subscription.entity.Subscription;
import com.team1.monew.subscription.repository.SubscriptionRepository;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NewArticlesCollectedEventAsyncTest {

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @Autowired
  private NotificationRepository notificationRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private InterestRepository interestRepository;

  @Autowired
  private SubscriptionRepository subscriptionRepository;

  @BeforeEach
  void setup() {
    notificationRepository.deleteAll();
    subscriptionRepository.deleteAll();
    userRepository.deleteAll();
    interestRepository.deleteAll();
  }

  @Test
  void NewArticlesCollectedEvent_shouldCallNotificationService_async() throws Exception {
    // given
    User user = userRepository.save(new User("test@email.com", "닉네임", "비밀번호"));
    Interest interest = interestRepository.save(new Interest("AI"));
    subscriptionRepository.save(new Subscription(user, interest));

    List<CollectedArticleDto> articleDtos = List.of(
        new CollectedArticleDto("제목1", "요약", "https://url", "네이버", LocalDateTime.now())
    );

    // when
    eventPublisher.publishEvent(new NewArticlesCollectedEvent(interest, articleDtos));

    // then (비동기 처리를 기다림)
    Awaitility.await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() -> {
          List<Notification> notiList = notificationRepository.findAll();
          assertThat(notiList).hasSize(1);
          assertThat(notiList.get(0).getUser().getId()).isEqualTo(user.getId());
          assertThat(notiList.get(0).getResourceType()).isEqualTo(ResourceType.INTEREST.getName());
        });
  }
}
