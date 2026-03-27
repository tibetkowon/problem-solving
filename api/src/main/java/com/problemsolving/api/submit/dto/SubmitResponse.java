package com.problemsolving.api.submit.dto;

import com.problemsolving.core.constant.AnswerStatus;
import com.problemsolving.core.domain.problem.entity.Choice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "문제 제출 응답")
public class SubmitResponse {

    @Schema(description = "문제 ID", example = "1")
    private final Long problemId;

    @Schema(description = "정답 여부 (CORRECT / PARTIAL / WRONG)")
    private final AnswerStatus answerStatus;

    @Schema(description = "문제 해설")
    private final String explanation;

    @Schema(description = "객관식 정답 선택지 ID 목록 (객관식만 반환)")
    private final List<Long> correctChoiceIds;

    @Schema(description = "주관식 정답 (주관식만 반환)")
    private final String correctAnswer;

    public static SubmitResponse ofObjective(Long problemId, AnswerStatus status,
                                              String explanation, List<Choice> correctChoices) {
        return SubmitResponse.builder()
                .problemId(problemId)
                .answerStatus(status)
                .explanation(explanation)
                .correctChoiceIds(correctChoices.stream().map(Choice::getId).toList())
                .build();
    }

    public static SubmitResponse ofSubjective(Long problemId, AnswerStatus status,
                                               String explanation, String correctAnswer) {
        return SubmitResponse.builder()
                .problemId(problemId)
                .answerStatus(status)
                .explanation(explanation)
                .correctAnswer(correctAnswer)
                .build();
    }
}
