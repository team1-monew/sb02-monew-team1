package com.team1.monew.notification;

import com.team1.monew.notification.dto.ResourceType;
import com.team1.monew.notification.service.NotificationService;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import com.team1.monew.notification.entity.Notification;
import com.team1.monew.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class NotificationIntegrationTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private NotificationRepository notificationRepository;

  private User testUser;

  @BeforeEach
  void setUp() {
    notificationRepository.deleteAll();
    userRepository.deleteAll();
    // Create a test user; adjust builder fields as per your User entity
    testUser = User.builder()
        .email("test@example.com")
        .nickname("testUser")
        .password("password")
        .build();
    testUser = userRepository.save(testUser);
  }

  @Test
  void testGetNotifications_empty() throws Exception {
    mockMvc.perform(get("/api/notifications")
            .header("Monew-Request-User-ID", testUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content").isEmpty());
  }

  @Test
  void testGetNotifications_withOne() throws Exception {
    Notification notification = Notification.builder()
        .user(testUser)
        .content("Test notification")
        .resourceType(ResourceType.COMMENT.getName())
        .resourceId(1L)
        .build();
    notificationRepository.save(notification);

    mockMvc.perform(get("/api/notifications")
            .header("Monew-Request-User-ID", testUser.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(notification.getId()))
        .andExpect(jsonPath("$.content[0].confirmed").value(false));
  }

  @Test
  void testMarkAllNotificationsAsRead() throws Exception {
    Notification n1 = Notification.builder().user(testUser).content("N1").resourceType(ResourceType.COMMENT.getName()).resourceId(1L).build();
    Notification n2 = Notification.builder().user(testUser).content("N2").resourceType(ResourceType.INTEREST.getName()).resourceId(2L).build();
    notificationRepository.save(n1);
    notificationRepository.save(n2);

    mockMvc.perform(patch("/api/notifications")
            .header("Monew-Request-User-ID", testUser.getId()))
        .andExpect(status().isOk());

    assertThat(notificationRepository.findAll())
        .allMatch(Notification::isConfirmed);
  }

  @Test
  void testMarkSingleNotificationAsRead() throws Exception {
    Notification notification = Notification.builder()
        .user(testUser)
        .content("Single")
        .resourceType(ResourceType.COMMENT.getName())
        .resourceId(3L)
        .build();
    notification = notificationRepository.save(notification);

    mockMvc.perform(patch("/api/notifications/{id}", notification.getId())
            .header("Monew-Request-User-ID", testUser.getId()))
        .andExpect(status().isOk());

    Notification updated = notificationRepository.findById(notification.getId()).orElseThrow();
    assertThat(updated.isConfirmed()).isTrue();
  }
}
