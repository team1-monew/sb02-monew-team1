package com.team1.monew.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentRegisterRequest(
    @NotNull(message = "기사 ID는 필수입니다.")
    Long articleId,

    @NotNull(message = "사용자 ID는 필수입니다.")
    Long userId,

    @NotBlank(message = "내용은 필수입니다.")
    @Size(min=1, max=500, message = "내용은 1자 이상 500자 이하이어야 합니다.")
    String content
) {

}
