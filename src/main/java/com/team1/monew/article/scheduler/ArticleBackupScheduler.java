package com.team1.monew.article.scheduler;

import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleBackupScheduler {

    private final JobLauncher jobLauncher;
    private final Job articleBackupJob;

    @Scheduled(cron = "0 30 0 * * *")
    public void runArticleBackupJob() {
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        log.info("📦 뉴스 기사 백업 작업 시작");

        JobExecution jobExecution;

        try {
            jobExecution = jobLauncher.run(articleBackupJob, params);
            log.info("✅ 뉴스 기사 백업 완료 - 상태: {}", jobExecution.getStatus());
        } catch (Exception e) {
            log.error("❌ 뉴스 기사 백업 실패", e);
            throw new RestException(ErrorCode.INTERNAL_SERVER_ERROR, Map.of(
                    "jobName", articleBackupJob.getName(),
                    "message", e.getMessage()
            ));
        }
    }
}
