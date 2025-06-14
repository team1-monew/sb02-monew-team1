package com.team1.monew.comment.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.team1.monew.article.entity.Article;
import com.team1.monew.comment.dto.CommentSearchCondition;
import com.team1.monew.comment.entity.Comment;
import com.team1.monew.config.QueryDslConfig;
import com.team1.monew.user.entity.User;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@DataJpaTest
@Import(QueryDslConfig.class)
public class CommentRepositoryImplTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager em;

    private Article article;
    private User user;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        baseTime = LocalDateTime.of(2023, 1, 1, 12, 0);

        user = new User("test@example.com", "닉네임", "pw");
        em.persist(user);

        article = Article.builder()
            .source("source")
            .sourceUrl("url")
            .title("title")
            .publishDate(baseTime)
            .summary("summary")
            .createdAt(baseTime)
            .build();
        em.persist(article);
    }

    @Test
    void createdAt_기준_desc_정렬() {
        Comment c1 = new Comment(article, user, "이전 댓글");
        Comment c2 = new Comment(article, user, "최근 댓글");
        em.persist(c1);
        em.persist(c2);
        em.flush();

        jdbcTemplate.update("UPDATE comments SET created_at = ? WHERE id = ?", Timestamp.valueOf(baseTime.minusMinutes(10)), c1.getId());
        jdbcTemplate.update("UPDATE comments SET created_at = ? WHERE id = ?", Timestamp.valueOf(baseTime), c2.getId());

        em.flush();
        em.clear();

        CommentSearchCondition condition = CommentSearchCondition.fromParams(
            article.getId(),
            "createdAt",
            "DESC",
            null,
            null,
            10,
            user.getId()
        );

        Slice<Comment> result = commentRepository.searchByCondition(condition);
        List<Comment> content = result.getContent();

        assertEquals(2, content.size());
        assertTrue(content.get(0).getCreatedAt().isAfter(content.get(1).getCreatedAt()));
    }

    @Test
    void likeCount_기준_asc_정렬_created_desc_정렬() {
        // given
        Comment c1 = new Comment(article, user, "like 5 - old");
        ReflectionTestUtils.setField(c1, "createdAt", LocalDateTime.of(2024, 1, 1, 0, 0));

        Comment c2 = new Comment(article, user, "like 5 - new");
        ReflectionTestUtils.setField(c2, "createdAt", LocalDateTime.of(2025, 1, 1, 0, 0));

        Comment c3 = new Comment(article, user, "like 10");
        ReflectionTestUtils.setField(c3, "createdAt", LocalDateTime.of(2023, 1, 1, 0, 0));
        em.persist(c1);
        em.persist(c2);
        em.persist(c3);
        em.flush();

        jdbcTemplate.update("UPDATE comments SET like_count = ? WHERE id = ?", 5L, c1.getId());
        jdbcTemplate.update("UPDATE comments SET like_count = ? WHERE id = ?", 5L, c2.getId());
        jdbcTemplate.update("UPDATE comments SET like_count = ? WHERE id = ?", 10L, c3.getId());

        em.flush();
        em.clear();

        CommentSearchCondition condition = CommentSearchCondition.fromParams(
            article.getId(),
            "likeCount",
            "ASC",
            null,
            null,
            10,
            user.getId()
        );

        Slice<Comment> result = commentRepository.searchByCondition(condition);
        List<Comment> content = result.getContent();

        assertEquals(3, content.size());

        // likeCount 기준 오름차순 정렬 확인
        assertTrue(content.get(0).getLikeCount() <= content.get(1).getLikeCount());
        assertTrue(content.get(1).getLikeCount() <= content.get(2).getLikeCount());

        // 동일한 likeCount(5) 내에서 createdAt DESC인지 (c2가 c1보다 앞에 와야 함)
        if (content.get(0).getLikeCount().equals(5L) &&
            content.get(1).getLikeCount().equals(5L)) {
            assertTrue(content.get(0).getCreatedAt().isAfter(content.get(1).getCreatedAt()));
        }

        // 마지막은 likeCount 10인 c3여야 함
        assertEquals(10L, content.get(2).getLikeCount());
    }

    @Test
    void 커서_이후_댓글만_조회된다() {
        Comment oldComment = new Comment(article, user, "이전");
        Comment newComment = new Comment(article, user, "이후");
        em.persist(oldComment);
        em.persist(newComment);
        em.flush();
        em.clear();

        jdbcTemplate.update("UPDATE comments SET created_at = ? WHERE id = ?", Timestamp.valueOf(baseTime.minusMinutes(10)), oldComment.getId());
        jdbcTemplate.update("UPDATE comments SET created_at = ? WHERE id = ?", Timestamp.valueOf(baseTime), newComment.getId());

        em.clear();

        CommentSearchCondition condition = CommentSearchCondition.fromParams(
            article.getId(),
            "createdAt",
            "ASC",
            baseTime.minusMinutes(10).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            baseTime.minusMinutes(10),
            10,
            user.getId()
        );

        Slice<Comment> result = commentRepository.searchByCondition(condition);
        assertEquals(1, result.getContent().size());
        assertEquals("이후", result.getContent().get(0).getContent());
    }

    @Test
    void 삭제댓글은_조회되지_않는다() {
        Comment c1 = new Comment(article, user, "살아있음");
        Comment c2 = new Comment(article, user, "삭제됨");
        em.persist(c1);
        em.persist(c2);
        em.flush();

        jdbcTemplate.update("UPDATE comments SET is_deleted = true WHERE id = ?", c2.getId());

        CommentSearchCondition condition = CommentSearchCondition.fromParams(
            article.getId(),
            "createdAt",
            "DESC",
            null,
            null,
            10,
            user.getId()
        );

        Slice<Comment> result = commentRepository.searchByCondition(condition);
        assertEquals(1, result.getContent().size());
        assertEquals("살아있음", result.getContent().get(0).getContent());
    }

    @Test
    void findTop10ByUserIdAndIsDeletedFalseOrderByCreatedAtDesc_정상조회() {
        // given
        for (int i = 0; i < 15; i++) {
            Comment comment = new Comment(article, user, "댓글 " + i);
            em.persist(comment);
        }
        em.flush();
        em.clear();

        // when
        List<Comment> result = commentRepository.findTop10ByUser_IdAndIsDeletedFalseOrderByCreatedAtDesc(
            user.getId(),
            Pageable.ofSize(10)
        );

        // then
        assertEquals(10, result.size());
        for (int i = 0; i < result.size() - 1; i++) {
            assertTrue(result.get(i).getCreatedAt().isAfter(result.get(i + 1).getCreatedAt()) ||
                result.get(i).getCreatedAt().isEqual(result.get(i + 1).getCreatedAt()));
        }
    }

    @Test
    void findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc_삭제댓글제외후_모두조회() {
        // given
        for (int i = 0; i < 5; i++) {
            Comment comment = new Comment(article, user, "살아있는 댓글 " + i);
            em.persist(comment);
        }
        Comment deleted = new Comment(article, user, "삭제된 댓글");
        em.persist(deleted);
        em.flush();

        jdbcTemplate.update("UPDATE comments SET is_deleted = true WHERE id = ?", deleted.getId());

        em.clear();

        // when
        List<Comment> result = commentRepository.findByUser_IdAndIsDeletedFalseOrderByCreatedAtDesc(user.getId());

        // then
        assertEquals(5, result.size());
        assertTrue(result.stream().noneMatch(c -> c.getContent().contains("삭제된")));
        for (int i = 0; i < result.size() - 1; i++) {
            assertTrue(result.get(i).getCreatedAt().isAfter(result.get(i + 1).getCreatedAt()) ||
                result.get(i).getCreatedAt().isEqual(result.get(i + 1).getCreatedAt()));
        }
    }
}
