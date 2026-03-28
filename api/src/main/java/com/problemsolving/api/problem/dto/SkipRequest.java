package com.problemsolving.api.problem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Schema(description = "문제 건너뛰기 요청")
public class SkipRequest {

    @NotNull(message = "사용자 ID는 필수입니다.")
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @NotNull(message = "문제 ID는 필수입니다.")
    @Schema(description = "건너뛸 문제 ID", example = "2")
    private Long problemId;

    @NotNull(message = "단원 ID는 필수입니다.")
    @Schema(description = "단원 ID", example = "1")
    private Long chapterId;
}
