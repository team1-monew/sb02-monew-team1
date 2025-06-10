package com.team1.monew.article.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.monew.article.dto.ArticleDto;
import com.team1.monew.article.repository.ArticleRepositoryCustom;
import com.team1.monew.common.S3Util;
import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@Slf4j
public class ArticleBackupJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ArticleRepositoryCustom articleRepositoryCustom;
    private final S3Util s3Util;
    private final ObjectMapper objectMapper;

    @Bean
    public Job articleBackupJob(Step articleBackupStep) {
        return new JobBuilder("articleBackupJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(articleBackupStep)
                .build();
    }

    @Bean
    public Step articleBackupStep(ItemReader<ArticleDto> reader,
                                  ItemProcessor<ArticleDto, ArticleDto> processor,
                                  ItemWriter<ArticleDto> writer) {
        return new StepBuilder("articleBackupStep", jobRepository)
                .<ArticleDto, ArticleDto>chunk(100, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public ItemReader<ArticleDto> reader() {
        List<ArticleDto> articles = articleRepositoryCustom.findAllCreatedYesterday();
        return new IteratorItemReader<>(articles);
    }

    @Bean
    public ItemProcessor<ArticleDto, ArticleDto> processor() {
        return articleDto -> articleDto;
    }

    @Bean
    public ItemWriter<ArticleDto> writer() {
        return articles -> {
            String key = "backup/articles/backup-articles-" + LocalDate.now().minusDays(1) + ".json";

            try {
                log.info("📦 배치 백업 파일 업로드 시작: key={}", key);
                String json = objectMapper.writeValueAsString(articles);
                byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(jsonBytes);

                s3Util.upload(key, inputStream, jsonBytes.length, "application/json");

                log.info("✅ 배치 백업 파일 업로드 완료: key={}", key);
            } catch (Exception e) {
                log.error("❌ 배치 백업 파일 업로드 중 오류 발생", e);
                throw new RestException(ErrorCode.IO_EXCEPTION, Map.of(
                        "key", key,
                        "message", e.getMessage()
                ));
            }
        };
    }
}
