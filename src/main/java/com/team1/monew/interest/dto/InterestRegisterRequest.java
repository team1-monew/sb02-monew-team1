package com.team1.monew.interest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record InterestRegisterRequest(
    @NotBlank(message = "관심사 이름을 입력해주세요.")
    @Size(max = 10, message = "관심사 이름은 10자 이하여야 합니다.")
    String name,

    @NotNull(message = "키워드 목록을 입력해주세요.")
    @Size(min = 1, message = "키워드는 최소 1개 이상 입력해야 합니다.")
    List<
        @NotBlank(message = "각 키워드는 공백이 아닌 문자열이어야 합니다.")
        @Size(max = 10, message = "키워드는 10자 이하여야 합니다.")
            String
        > keywords
)
{

}
