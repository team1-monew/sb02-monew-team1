package com.team1.monew.comment.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.team1.monew.article.entity.Article;
import com.team1.monew.comment.entity.Comment;
import com.team1.monew.comment.entity.CommentLike;
import com.team1.monew.config.QueryDslConfig;
import com.team1.monew.user.entity.User;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
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

}
