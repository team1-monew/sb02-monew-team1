package com.team1.monew.comment.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.team1.monew.article.entity.Article;
import com.team1.monew.comment.entity.Comment;
import com.team1.monew.comment.entity.CommentLike;
import com.team1.monew.user.entity.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
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

        Article article = new Article("source", "url", "제목", LocalDateTime.now(), "요약");
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

        Article article = new Article("source", "url", "제목", LocalDateTime.now(), "요약");
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
}
