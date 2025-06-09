package com.team1.monew.useractivity.entity;

import com.team1.monew.article.dto.ArticleViewDto;
import com.team1.monew.comment.dto.CommentActivityDto;
import com.team1.monew.comment.dto.CommentDto;
import com.team1.monew.comment.dto.CommentLikeActivityDto;
import com.team1.monew.comment.entity.Comment;
import com.team1.monew.subscription.dto.SubscriptionDto;
import com.team1.monew.user.dto.UserDto;
import com.team1.monew.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "user_activities")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@Getter
public class UserActivity {

    @Id
    private Long id;

    private UserDto user;

    private List<CommentActivityDto> commentList = new ArrayList<>();

    private List<CommentLikeActivityDto> commentLikeList = new ArrayList<>();

    private List<ArticleViewDto> articleViewList = new ArrayList<>();

    private List<SubscriptionDto> subscriptionList = new ArrayList<>();

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updateAt;
}
