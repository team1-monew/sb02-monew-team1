package com.team1.monew.interest.dto;

import java.time.Instant;

public record InterestSearchCondition(
    String keyword,
    Instant after,
    String cursor,
    int limit,
    String orderBy,
    String direction
) {

}
