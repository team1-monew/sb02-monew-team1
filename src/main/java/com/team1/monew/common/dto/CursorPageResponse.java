package com.team1.monew.common.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CursorPageResponse<T>(
    List<T> content,
    String nextCursor,
    LocalDateTime nextAfter,
    Long size,
    Long totalElements,
    boolean hasNext
) {

}