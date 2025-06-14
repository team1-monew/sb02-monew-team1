package com.team1.monew.comment.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.team1.monew.article.entity.Article;
import com.team1.monew.comment.entity.Comment;
import com.team1.monew.comment.entity.CommentLike;
import com.team1.monew.config.QueryDslConfig;
import com.team1.monew.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@Import(QueryDslConfig.class)
public class CommentLikeRepositoryTest {

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void existsByCommentIdAndLikedBy_존재하면_true반환() {
        // given
        User user = new User("test@example.com", "닉네임", "암호");
        entityManager.persist(user);

        Article article = Article.builder()
            .source("source")
            .sourceUrl("url")
            .title("제목")
            .publishDate(LocalDateTime.now())
            .summary("요약")
            .createdAt(LocalDateTime.now())
            .build();
        entityManager.persist(article);

        Comment comment = new Comment(article, user, "댓글 내용");
        entityManager.persist(comment);

        CommentLike like = new CommentLike(comment, user);
        entityManager.persist(like);

        entityManager.flush();
        entityManager.clear();

        // when
        boolean exists = commentLikeRepository.existsByComment_IdAndLikedBy_Id(comment.getId(), user.getId());

        // then
        assertTrue(exists);
    }


    @Test
    void existsByCommentIdAndLikedBy_존재하지않으면_false반환() {
        // given
        User user = new User("test2@example.com", "다른유저", "암호");
        entityManager.persist(user);

        Article article = Article.builder()
            .source("source")
            .sourceUrl("url")
            .title("제목")
            .publishDate(LocalDateTime.now())
            .summary("요약")
            .createdAt(LocalDateTime.now())
            .build();
        entityManager.persist(article);

        Comment comment = new Comment(article, user, "다른 댓글 내용");
        entityManager.persist(comment);

        entityManager.flush();
        entityManager.clear();

        // when
        boolean exists = commentLikeRepository.existsByComment_IdAndLikedBy_Id(comment.getId(), user.getId());

        // then
        assertFalse(exists);
    }

    @Test
    void countByCommentId_좋아요개수반환() {
        // given
        User user1 = new User("user1@example.com", "유저1", "암호");
        User user2 = new User("user2@example.com", "유저2", "암호");
        entityManager.persist(user1);
        entityManager.persist(user2);

        Article article = Article.builder()
            .source("source")
            .sourceUrl("url")
            .title("제목")
            .publishDate(LocalDateTime.now())
            .summary("요약")
            .createdAt(LocalDateTime.now())
            .build();
        entityManager.persist(article);

        Comment comment = new Comment(article, user1, "댓글 내용");
        entityManager.persist(comment);

        CommentLike like1 = new CommentLike(comment, user1);
        CommentLike like2 = new CommentLike(comment, user2);
        entityManager.persist(like1);
        entityManager.persist(like2);

        entityManager.flush();
        entityManager.clear();

        // when
        Long likeCount = commentLikeRepository.countByCommentId(comment.getId());

        // then
        assertTrue(likeCount == 2L);
    }

    @Test
    void findByCommentIdAndLikedById_존재하면_Optional반환() {
        // given
        User user = new User("test3@example.com", "유저3", "암호");
        entityManager.persist(user);

        Article article = Article.builder()
            .source("source")
            .sourceUrl("url")
            .title("제목")
            .publishDate(LocalDateTime.now())
            .summary("요약")
            .createdAt(LocalDateTime.now())
            .build();
        entityManager.persist(article);

        Comment comment = new Comment(article, user, "댓글 내용");
        entityManager.persist(comment);

        CommentLike like = new CommentLike(comment, user);
        entityManager.persist(like);

        entityManager.flush();
        entityManager.clear();

        // when
        Optional<CommentLike> result = commentLikeRepository.findByComment_IdAndLikedBy_Id(comment.getId(), user.getId());

        // then
        assertTrue(result.isPresent());
    }

    @Test
    void findByCommentIdAndLikedById_존재하지않으면_빈Optional반환() {
        // given
        User user = new User("test4@example.com", "유저4", "암호");
        entityManager.persist(user);

        Article article = Article.builder()
            .source("source")
            .sourceUrl("url")
            .title("제목")
            .publishDate(LocalDateTime.now())
            .summary("요약")
            .createdAt(LocalDateTime.now())
            .build();
        entityManager.persist(article);

        Comment comment = new Comment(article, user, "댓글 내용");
        entityManager.persist(comment);

        entityManager.flush();
        entityManager.clear();

        // when
        Optional<CommentLike> result = commentLikeRepository.findByComment_IdAndLikedBy_Id(comment.getId(), user.getId());

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void findTop10WithCommentByLikedById_최신순10개조회() {
        // given
        User user = new User("top10@example.com", "Top10", "pw");
        entityManager.persist(user);

        Article article = Article.builder()
            .source("source")
            .sourceUrl("url")
            .title("제목")
            .publishDate(LocalDateTime.now())
            .summary("요약")
            .createdAt(LocalDateTime.now())
            .build();
        entityManager.persist(article);

        // 15개 좋아요 등록
        for (int i = 0; i < 15; i++) {
            Comment comment = new Comment(article, user, "댓글 " + i);
            entityManager.persist(comment);

            CommentLike like = new CommentLike(comment, user);
            entityManager.persist(like);
        }

        entityManager.flush();
        entityManager.clear();

        // when
        List<CommentLike> result = commentLikeRepository.findTop10WithCommentByLikedById(user.getId(), Pageable.ofSize(10));

        // then
        assertEquals(10, result.size());

        // 최신순 정렬 검증
        for (int i = 0; i < result.size() - 1; i++) {
            LocalDateTime cur = result.get(i).getCreatedAt();
            LocalDateTime next = result.get(i + 1).getCreatedAt();
            assertTrue(cur.isAfter(next) || cur.isEqual(next));
        }

        // 연관된 댓글과 기사가 페치조인 되었는지 검증
        assertTrue(result.stream().allMatch(cl -> cl.getComment() != null && cl.getComment().getArticle() != null));
    }

    @Test
    void findWithCommentByLikedById_모든좋아요조회() {
        // given
        User user = new User("alllikes@example.com", "AllLikes", "pw");
        entityManager.persist(user);

        Article article = Article.builder()
            .source("source")
            .sourceUrl("url")
            .title("제목")
            .publishDate(LocalDateTime.now())
            .summary("요약")
            .createdAt(LocalDateTime.now())
            .build();
        entityManager.persist(article);

        // 3개 좋아요 등록
        for (int i = 0; i < 3; i++) {
            Comment comment = new Comment(article, user, "댓글 " + i);
            entityManager.persist(comment);

            CommentLike like = new CommentLike(comment, user);
            entityManager.persist(like);
        }

        entityManager.flush();
        entityManager.clear();

        // when
        List<CommentLike> result = commentLikeRepository.findWithCommentByLikedById(user.getId());

        // then
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(cl -> cl.getComment() != null && cl.getComment().getArticle() != null));
    }
}
