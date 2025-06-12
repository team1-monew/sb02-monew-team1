package com.team1.monew.notification.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class NotificationJobScheduler {

  private final JobLauncher jobLauncher;
  private final Job notificationCleanupJob;

  /**
   * 매일 새벽 2시에 실행
   */
  @Scheduled(cron = "0 0 2 * * *")
  public void runCleanupJob() throws Exception {
    jobLauncher.run(notificationCleanupJob, new JobParameters());
  }
}