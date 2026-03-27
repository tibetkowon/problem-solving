package com.problemsolving.api.problem.dto;

import com.problemsolving.core.constant.ProblemType;
import com.problemsolving.core.domain.problem.entity.Choice;
import com.problemsolving.core.domain.problem.entity.Problem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "랜덤 문제 조회 응답")
public class RandomProblemResponse {

    @Schema(description = "문제 ID", example = "1")
    private final Long problemId;

    @Schema(description = "문제 내용")
    private final String content;

    @Schema(description = "문제 타입")
    private final ProblemType problemType;

    @Schema(description = "선택지 목록 (객관식만 존재)")
    private final List<ChoiceDto> choices;

    @Schema(description = "정답률 (30명 이상 제출 시에만 반환, 미만이면 null)", example = "67")
    private final Integer answerCorrectRate;

    public RandomProblemResponse(Problem problem, List<Choice> choices, Integer answerCorrectRate) {
        this.problemId = problem.getId();
        this.content = problem.getContent();
        this.problemType = problem.getType();
        this.choices = choices.stream().map(ChoiceDto::new).toList();
        this.answerCorrectRate = answerCorrectRate;
    }

    @Getter
    @Schema(description = "선택지")
    public static class ChoiceDto {
        @Schema(description = "선택지 ID", example = "1")
        private final Long id;

        @Schema(description = "선택지 내용", example = "스택")
        private final String content;

        public ChoiceDto(Choice choice) {
            this.id = choice.getId();
            this.content = choice.getContent();
        }
    }
}
