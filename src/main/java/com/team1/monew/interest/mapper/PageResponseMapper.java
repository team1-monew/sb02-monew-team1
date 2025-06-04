package com.team1.monew.interest.mapper;

import com.team1.monew.common.dto.CursorPageResponse;
import com.team1.monew.interest.dto.InterestDto;
import com.team1.monew.interest.dto.InterestSearchCondition;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
public class PageResponseMapper {
  public CursorPageResponse<InterestDto> toPageResponse(
      Slice<InterestDto> interestDtoSlice,
      InterestSearchCondition interestSearchCondition) {

    List<InterestDto> content = interestDtoSlice.getContent();
    InterestDto last = content.isEmpty() ? null : content.get(content.size() - 1);

    String nextCursor = null;
    if (last != null) {
      nextCursor = "name".equalsIgnoreCase(interestSearchCondition.orderBy())
          ? last.name()
          : last.subscriberCount().toString();
    }

    LocalDateTime nextAfter = null;
    if (last != null) {
      nextAfter = last.createdAt();
    }

    return new CursorPageResponse<>(
        content,
        nextCursor,
        nextAfter,
        (long) interestSearchCondition.limit(),
        null,
        interestDtoSlice.hasNext()
    );
  }
}
