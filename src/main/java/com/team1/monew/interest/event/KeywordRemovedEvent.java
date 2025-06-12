package com.team1.monew.interest.event;

import com.team1.monew.interest.entity.Interest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class KeywordRemovedEvent {

    private final Interest interest;
    private final String keyword;
}
