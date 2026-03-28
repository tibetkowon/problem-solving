package com.problemsolving.api.problem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Schema(description = "랜덤 문제 조회 요청")
public class RandomProblemRequest {

    @NotNull(message = "단원 ID는 필수입니다.")
    @Schema(description = "단원 ID", example = "1")
    private Long chapterId;

    @NotNull(message = "사용자 ID는 필수입니다.")
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "건너뛸 문제 ID (선택). 전달 시 해당 문제를 제외하고 다음 문제를 반환합니다.", example = "2")
    private Long skipProblemId;
}
