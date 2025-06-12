package com.team1.monew.notification.config;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import com.team1.monew.notification.repository.NotificationRepository;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@Slf4j
public class NotificationCleanupJobConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final NotificationRepository notificationRepository;

  @Bean
  public Job notificationCleanupJob(Step notificationCleanupStep) {
    return new JobBuilder("notificationCleanupJob", jobRepository)
        .incrementer(new RunIdIncrementer())
        .start(notificationCleanupStep)
        .build();
  }

  @Bean
  public Step notificationCleanupStep() {
    return new StepBuilder("notificationCleanupStep", jobRepository)
        .<Integer, Integer>chunk(1, transactionManager)
        .reader(() -> {
          // 청크당 한 번만 실행하도록, 더이상 읽을 게 없으면 null 반환
          LocalDateTime cutoff = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
          int deleted = notificationRepository.deleteConfirmedBefore(cutoff);
          log.info("1주일 지난 confirmed 알림 삭제: {}건", deleted);
          return null;  // 한번만 실행
        })
        .processor(item -> item)   // 통과
        .writer(list -> {})        // reader 안에서 이미 삭제 처리했으므로 writer는 빈 구현
        .build();
  }
}