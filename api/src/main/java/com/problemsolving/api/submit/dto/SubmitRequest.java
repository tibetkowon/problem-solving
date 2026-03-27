package com.problemsolving.api.submit.dto;

import com.problemsolving.core.constant.ProblemType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "문제 제출 요청")
public class SubmitRequest {

    @NotNull(message = "문제 ID는 필수입니다.")
    @Schema(description = "문제 ID", example = "1")
    private Long problemId;

    @NotNull(message = "사용자 ID는 필수입니다.")
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @NotNull(message = "문제 타입은 필수입니다.")
    @Schema(description = "문제 타입")
    private ProblemType answerType;

    @Schema(description = "주관식 답변 (주관식 문제만 사용)", example = "스택")
    private String subjectiveAnswer;

    @Schema(description = "객관식 선택지 ID 목록 (객관식 문제만 사용)", example = "[1, 3]")
    private List<Long> selectedChoiceIds;
}
