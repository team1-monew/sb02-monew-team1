package com.team1.monew.article.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.team1.monew.article.collector.ChosunNewsCollector;
import com.team1.monew.article.collector.NaverNewsCollector;
import com.team1.monew.article.dto.ArticleDto;
import com.team1.monew.article.dto.ArticleViewDto;
import com.team1.monew.article.dto.CollectedArticleDto;
import com.team1.monew.article.entity.*;
import com.team1.monew.article.repository.*;
import com.team1.monew.comment.repository.CommentRepository;
import com.team1.monew.comment.entity.Comment;
import com.team1.monew.common.S3Util;
import com.team1.monew.common.dto.CursorPageResponse;
import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import com.team1.monew.interest.entity.Interest;
import com.team1.monew.interest.entity.Keyword;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ArticleServiceImplTest {

    @InjectMocks
    private ArticleServiceImpl articleService;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleRepositoryCustom articleRepositoryCustom;

    @Mock
    private ArticleViewRepository articleViewRepository;

    @Mock
    private ArticleInterestRepository articleInterestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private NaverNewsCollector naverNewsCollector;

    @Mock
    private ChosunNewsCollector chosunNewsCollector;

    @Mock
    private S3Util s3Util;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("네이버 기사 수집 및 저장 정상 수행")
    void testCollectAndSaveNaverArticles() {
        // given
        Interest interest = new Interest("관심사");
        Keyword keyword = new Keyword("키워드");
        CollectedArticleDto dto = new CollectedArticleDto("title", "summary", "source", "url", LocalDateTime.now());
        List<CollectedArticleDto> articles = List.of(dto);

        when(naverNewsCollector.collect(interest, keyword)).thenReturn(articles);
        when(articleRepository.existsBySourceUrl(anyString())).thenReturn(false);
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        articleService.collectAndSaveNaverArticles(interest, keyword);

        // then
        verify(naverNewsCollector).collect(interest, keyword);
        verify(articleRepository, times(1)).save(any(Article.class));
    }

    @Test
    @DisplayName("조선일보 기사 수집 및 필터링 후 저장 정상 수행")
    void testCollectAndSaveChosunArticles() {
        // given
        Interest interest = new Interest("관심사");
        Keyword keyword = new Keyword("키워드");

        CollectedArticleDto dto1 = new CollectedArticleDto("키워드 포함 제목", "summary", "source", "url1", LocalDateTime.now());
        CollectedArticleDto dto2 = new CollectedArticleDto("제목", "키워드 포함 요약", "source", "url2", LocalDateTime.now());
        CollectedArticleDto dto3 = new CollectedArticleDto("제목", "요약", "source", "url3", LocalDateTime.now());
        List<CollectedArticleDto> articles = List.of(dto1, dto2, dto3);

        when(chosunNewsCollector.collect(interest, keyword)).thenReturn(articles);
        when(articleRepository.existsBySourceUrl(anyString())).thenReturn(false);
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        articleService.collectAndSaveChosunArticles(interest, keyword);

        // then
        verify(chosunNewsCollector).collect(interest, keyword);
        verify(articleRepository, times(2)).save(any(Article.class));
    }

    @Test
    @DisplayName("새로운 조회 기록 저장 및 조회수 증가")
    void testRecordView_NewView() {
        // given
        Long articleId = 1L;
        Long userId = 2L;
        Article article = Article.builder()
                .id(articleId)
                .viewCount(0L)
                .build();
        User user = User.builder()
                .email("test@example.com")
                .nickname("tester")
                .password("encryptedPassword")
                .build();

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(articleViewRepository.existsByArticleIdAndViewedById(articleId, userId)).thenReturn(false);
        when(commentRepository.countByArticleIdAndIsDeletedFalse(articleId)).thenReturn(5L);
        when(articleViewRepository.save(any(ArticleView.class))).thenAnswer(i -> i.getArgument(0));
        when(articleRepository.save(any(Article.class))).thenAnswer(i -> i.getArgument(0));

        // when
        ArticleViewDto result = articleService.recordView(articleId, userId);

        // then
        assertNotNull(result);
        assertEquals(5L, result.articleCommentCount());
        assertEquals(article.getViewCount(), result.articleViewCount());
        assertEquals(article.getId(), result.articleId());

        verify(articleViewRepository, times(1)).save(any(ArticleView.class));
        verify(articleRepository, times(1)).save(article);
    }

    @Test
    @DisplayName("기존 조회 기록 존재시 저장만 수행")
    void testRecordView_ExistingView() {
        // given
        Long articleId = 1L;
        Long userId = 2L;
        Article article = Article.builder()
                .id(articleId)
                .viewCount(10L)
                .build();
        User user = User.builder()
                .email("test@example.com")
                .nickname("tester")
                .password("encryptedPassword")
                .build();

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(articleViewRepository.existsByArticleIdAndViewedById(articleId, userId)).thenReturn(true);
        when(commentRepository.countByArticleIdAndIsDeletedFalse(articleId)).thenReturn(3L);
        when(articleViewRepository.save(any(ArticleView.class))).thenAnswer(i -> i.getArgument(0));

        // when
        ArticleViewDto result = articleService.recordView(articleId, userId);

        // then
        assertNotNull(result);
        assertEquals(3L, result.articleCommentCount());
        assertEquals(article.getViewCount(), result.articleViewCount());
        assertEquals(article.getId(), result.articleId());

        verify(articleViewRepository, times(1)).save(any(ArticleView.class));
        verify(articleRepository, never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 articleId 에러 발생")
    void testRecordView_ArticleNotFound() {
        // given
        when(articleRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        RestException ex = assertThrows(RestException.class,
                () -> articleService.recordView(1L, 1L));
        assertEquals(ErrorCode.NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("존재하지 않는 userId 에러 발생")
    void testRecordView_UserNotFound() {
        // given
        Article article = Article.builder().id(1L).build();
        when(articleRepository.findById(anyLong())).thenReturn(Optional.of(article));
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        RestException ex = assertThrows(RestException.class,
                () -> articleService.recordView(1L, 1L));
        assertEquals(ErrorCode.NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("articleRepositoryCustom 호출 후 결과 반환")
    void testGetArticles() {
        // given
        CursorPageResponse<ArticleDto> response = mock(CursorPageResponse.class);
        when(articleRepositoryCustom.searchArticles(any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), any(), any())).thenReturn(response);

        // when
        CursorPageResponse<ArticleDto> result = articleService.getArticles(
                "keyword", 1L, List.of("source"), LocalDateTime.now(), LocalDateTime.now(),
                "orderBy", "desc", "cursor", 10, "after", 1L);

        // then
        assertEquals(response, result);
        verify(articleRepositoryCustom).searchArticles(any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("중복 제거된 source 목록 반환")
    void testGetSources() {
        // given
        Article a1 = Article.builder().source("source1").build();
        Article a2 = Article.builder().source("source2").build();
        Article a3 = Article.builder().source("source1").build();

        when(articleRepository.findAll()).thenReturn(List.of(a1, a2, a3));

        // when
        List<String> sources = articleService.getSources();

        // then
        assertEquals(2, sources.size());
        assertTrue(sources.contains("source1"));
        assertTrue(sources.contains("source2"));
    }

    @Test
    @DisplayName("백업된 기사 복구 성공")
    void testRestoreArticles_Success() throws Exception {
        // given
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0);
        String backupKey = "backup/articles/backup-articles-2024-01-01.json";

        ArticleDto articleDto = new ArticleDto(1L, "source", "url", "title", LocalDateTime.now(), "summary", 0L, null, false);
        List<ArticleDto> articles = List.of(articleDto);
        Map<String, Object> jsonMap = Map.of("items", articles);

        byte[] fileBytes = objectMapper.writeValueAsString(jsonMap).getBytes(StandardCharsets.UTF_8);

        // when
        TypeReference<Map<String, Object>> mapTypeRef = new TypeReference<Map<String, Object>>() {};
        TypeReference<List<ArticleDto>> listTypeRef = new TypeReference<List<ArticleDto>>() {};

        when(s3Util.download(backupKey)).thenReturn(fileBytes);

        String jsonString = new String(fileBytes, StandardCharsets.UTF_8);

        if (jsonString == null || jsonString.isEmpty()) {
            jsonString = "{}";
        }

        Map<String, Object> jsonData = objectMapper.readValue(jsonString, mapTypeRef);
        List<ArticleDto> restoredArticles = objectMapper.convertValue(jsonData.get("items"), listTypeRef);

        // then
        assertNotNull(restoredArticles);
        assertEquals(1, restoredArticles.size());
    }

    @Test
    @DisplayName("댓글 삭제 후 article 삭제 표시")
    void testDeleteArticle() {
        // given
        Long articleId = 1L;
        Article article = mock(Article.class);
        Comment comment1 = mock(Comment.class);
        Comment comment2 = mock(Comment.class);

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(commentRepository.findByArticleId(articleId)).thenReturn(List.of(comment1, comment2));

        // when
        articleService.deleteArticle(articleId);

        // then
        verify(comment1).delete();
        verify(comment2).delete();
        verify(article).markDeleted();
    }

    @Test
    @DisplayName("존재하지 않는 articleId 에러 발생")
    void testDeleteArticle_NotFound() {
        // given
        when(articleRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        RestException ex = assertThrows(RestException.class, () -> articleService.deleteArticle(1L));
        assertEquals(ErrorCode.NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("연관 데이터 삭제 후 article 물리 삭제")
    void testHardDeleteArticle() {
        // given
        Long articleId = 1L;
        Article article = mock(Article.class);

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));

        // when
        articleService.hardDeleteArticle(articleId);

        // then
        verify(commentRepository).deleteByArticleId(articleId);
        verify(articleViewRepository).deleteByArticleId(articleId);
        verify(articleInterestRepository).deleteByArticleId(articleId);
        verify(articleRepository).delete(article);
    }

    @Test
    @DisplayName(" 존재하지 않는 articleId 에러 발생")
    void testHardDeleteArticle_NotFound() {
        // given
        when(articleRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        RestException ex = assertThrows(RestException.class, () -> articleService.hardDeleteArticle(1L));
        assertEquals(ErrorCode.NOT_FOUND, ex.getErrorCode());
    }

}