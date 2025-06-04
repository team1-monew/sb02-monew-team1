package com.team1.monew.comment.dto;

import com.team1.monew.exception.ErrorCode;
import com.team1.monew.exception.RestException;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CommentOrderBy {
    CREATED_AT("createdAt"),
    LIKE_COUNT("likeCount");

    private final String value;

    public static CommentOrderBy from(String value) {
        return switch (value) {
            case "createdAt" -> CREATED_AT;
            case "likeCount" -> LIKE_COUNT;
            default -> throw new RestException(ErrorCode.ENUM_TYPE_INVALID, Map.of("commentOrderBy", value, "details", "Invalid comment order by value"));
        };
    }
}
