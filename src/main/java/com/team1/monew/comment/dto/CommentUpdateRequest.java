package com.team1.monew.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentUpdateRequest(
    @NotBlank(message = "내용은 필수입니다.")
    @Size(min=1, max=500, message = "내용은 1자 이상 500자 이하이어야 합니다.")
    String content
) {

}
