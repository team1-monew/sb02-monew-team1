package com.team1.monew.common.dto;

import java.time.Instant;
import java.util.List;

public record CursorPageResponse<T>(
    List<T> content,
    String nextCursor,
    Instant nextAfter,
    Long size,
    Long totalElements,
    boolean hasNext
) {

}