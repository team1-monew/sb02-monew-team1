package com.team1.monew.article.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.monew.article.dto.ArticleDto;
import com.team1.monew.article.repository.ArticleRepositoryCustom;
import com.team1.monew.common.S3Util;
import com.team1.monew.exception.RestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

class ArticleBackupJobConfigTest {

    @Mock
    private ArticleRepositoryCustom articleRepositoryCustom;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private S3Util s3Util;

    @Mock
    private ObjectMapper objectMapper;

    private ArticleBackupJobConfig articleBackupJobConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        articleBackupJobConfig = new ArticleBackupJobConfig(jobRepository, transactionManager, articleRepositoryCustom, s3Util, objectMapper);
    }

    @Test
    @DisplayName("articleBackupJob가 step과 incrementer와 함께 올바르게 구성되는지 확인")
    void testArticleBackupJobConfiguration() throws Exception {
        // Given
        Step articleBackupStep = mock(Step.class);
        Job articleBackupJob = articleBackupJobConfig.articleBackupJob(articleBackupStep);

        when(jobRepository.getLastJobExecution(anyString(), any(JobParameters.class))).thenReturn(null);

        JobInstance mockJobInstance = mock(JobInstance.class);
        when(mockJobInstance.getJobName()).thenReturn("articleBackupJob");

        JobExecution mockJobExecution = mock(JobExecution.class);
        when(mockJobExecution.getExitStatus()).thenReturn(ExitStatus.COMPLETED);
        when(mockJobExecution.getJobInstance()).thenReturn(mockJobInstance);

        when(jobLauncher.run(eq(articleBackupJob), any(JobParameters.class))).thenReturn(mockJobExecution);

        // When
        JobExecution jobExecution = jobLauncher.run(articleBackupJob, new JobParameters());

        // Then
        assertNotNull(jobExecution);
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
        assertNotNull(jobExecution.getJobInstance());
        assertEquals("articleBackupJob", jobExecution.getJobInstance().getJobName());
    }

    @Test
    @DisplayName("articleBackupStep이 생성되고 이름이 올바른지 확인")
    void testArticleBackupStep() {
        // Given
        ItemReader<ArticleDto> mockReader = mock(ItemReader.class);
        ItemProcessor<ArticleDto, ArticleDto> mockProcessor = mock(ItemProcessor.class);
        ItemWriter<ArticleDto> mockWriter = mock(ItemWriter.class);

        // When
        Step step = articleBackupJobConfig.articleBackupStep(mockReader, mockProcessor, mockWriter);

        // Then
        assertNotNull(step);
        assertEquals("articleBackupStep", step.getName());
    }

    @Test
    @DisplayName("어제 생성된 기사 읽어오는지 확인")
    void testArticleReader() throws Exception {
        // Given
        ArticleDto article = new ArticleDto(1L, "source", "sourceUrl", "title", LocalDateTime.now(), "summary", 5L, 100L, false);
        List<ArticleDto> articles = Arrays.asList(article);
        when(articleRepositoryCustom.findAllCreatedYesterday()).thenReturn(articles);

        // When
        ItemReader<ArticleDto> reader = articleBackupJobConfig.reader();
        ArticleDto result = reader.read();

        // Then
        assertNotNull(result);
        assertEquals(article, result);
    }

    @Test
    @DisplayName("Processor가 ArticleDto를 그대로 반환하는지 확인")
    void testItemProcessor() throws Exception {
        // Given
        ArticleDto article = new ArticleDto(1L, "source", "sourceUrl", "title", LocalDateTime.now(), "summary", 5L, 100L, false);

        // When
        ItemProcessor<ArticleDto, ArticleDto> processor = articleBackupJobConfig.processor();
        ArticleDto result = processor.process(article);

        // Then
        assertNotNull(result);
        assertEquals(article, result);
    }


    @Test
    @DisplayName("S3 업로드 실패 시 RestException 발생")
    void testArticleWriter_RestException() throws Exception {
        // Given
        ArticleDto article = new ArticleDto(1L, "source", "sourceUrl", "title", LocalDateTime.now(), "summary", 5L, 100L, false);
        List<ArticleDto> articles = Arrays.asList(article);

        String json = "[{\"id\":1,\"source\":\"source\",\"sourceUrl\":\"sourceUrl\",\"title\":\"title\",\"createdAt\":\"2025-06-10T00:00:00\",\"summary\":\"summary\",\"viewCount\":5,\"commentCount\":100,\"isDeleted\":false}]";

        when(objectMapper.writeValueAsString(articles)).thenReturn(json);

        doThrow(new RuntimeException("S3 upload failed")).when(s3Util).upload(anyString(), any(), anyInt(), anyString());

        // when & then
        RestException thrownException = assertThrows(RestException.class, () -> {
            articleBackupJobConfig.writer().write(new Chunk<>(articles));
        });

        assertEquals("IO_EXCEPTION", thrownException.getErrorCode().toString());
    }
}
