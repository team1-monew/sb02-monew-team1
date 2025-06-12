package com.team1.monew.useractivity.scheduler.service;

import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.WriteModel;
import com.team1.monew.comment.dto.CommentLikeActivityDto;
import com.team1.monew.comment.entity.CommentLike;
import com.team1.monew.comment.repository.CommentLikeRepository;
import com.team1.monew.user.entity.User;
import com.team1.monew.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class CommentLikeActivityBatchService {

    private final UserRepository userRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final MongoTemplate mongoTemplate;

    @Transactional
    public void syncAll(){
        log.info("[배치 시작] 좋아요한 댓글 MongoDB 동기화");

        List<User> users = userRepository.findAll();
        List<WriteModel<Document>> operations = new ArrayList<>();

        for (User user : users){
            try {
                List<CommentLike> comments = commentLikeRepository.findWithCommentByLikedById(user.getId());

                List<CommentLikeActivityDto> commentLikeDtos = comments.stream()
                    .map(commentLike -> CommentLikeActivityDto.builder()
                        .id(commentLike.getId())
                        .createdAt(commentLike.getCreatedAt())
                        .commentId(commentLike.getComment().getId())
                        .articleId(commentLike.getComment().getArticle().getId())
                        .articleTitle(commentLike.getComment().getArticle().getTitle())
                        .commentUserId(commentLike.getComment().getUser().getId())
                        .commentUserNickname(commentLike.getComment().getUser().getNickname())
                        .commentContent(commentLike.getComment().getContent())
                        .commentLikeCount(commentLike.getComment().getLikeCount())
                        .commentCreatedAt(commentLike.getComment().getCreatedAt())
                        .build())
                    .toList();

                Document document = new Document();
                document.put("_id", user.getId());
                document.put("commentLikes", commentLikeDtos);
                document.put("createdAt", LocalDateTime.now());
                document.put("updatedAt", LocalDateTime.now());

                Query query = Query.query(Criteria.where("_id").is(user.getId()));
                ReplaceOneModel<Document> replaceModel = new ReplaceOneModel<>(
                    query.getQueryObject(),
                    document,
                    new ReplaceOptions().upsert(true)
                );

                operations.add(replaceModel);
            } catch (Exception e) {
                log.error("좋아요한 댓글 활동 동기화 실패 - userId: {}, reason: {}", user.getId(), e.getMessage(), e);
            }
        }

        if (!operations.isEmpty()) {
            mongoTemplate.getCollection("comment_like_activities")
                .bulkWrite(operations, new BulkWriteOptions().ordered(false));
            log.info("총 {}건의 CommentLikeActivity 문서가 bulkWrite 되었습니다.", operations.size());
        }

        log.info("[배치 종료] 좋아요한 댓글 MongoDB 동기화 완료");
    }
}
