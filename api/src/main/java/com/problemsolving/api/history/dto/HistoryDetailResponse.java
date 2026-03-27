package com.problemsolving.api.history.dto;

import com.problemsolving.core.constant.AnswerStatus;
import com.problemsolving.core.constant.ProblemType;
import com.problemsolving.core.domain.problem.entity.Choice;
import com.problemsolving.core.domain.userproblem.entity.UserProblem;
import com.problemsolving.core.domain.userproblem.entity.UserProblemChoice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "풀었던 문제 상세 응답")
public class HistoryDetailResponse {

    @Schema(description = "문제 ID", example = "1")
    private final Long problemId;

    @Schema(description = "문제 타입")
    private final ProblemType problemType;

    @Schema(description = "정답 여부")
    private final AnswerStatus answerStatus;

    @Schema(description = "문제 해설")
    private final String explanation;

    @Schema(description = "객관식 정답 선택지 ID 목록")
    private final List<Long> correctChoiceIds;

    @Schema(description = "주관식 정답")
    private final String correctAnswer;

    @Schema(description = "사용자가 제출한 객관식 선택지 ID 목록")
    private final List<Long> userChoiceIds;

    @Schema(description = "사용자가 제출한 주관식 답변")
    private final String userSubjectiveAnswer;

    @Schema(description = "정답률 (30명 이상 제출 시에만 반환)", example = "67")
    private final Integer answerCorrectRate;

    public static HistoryDetailResponse of(UserProblem userProblem,
                                           List<Choice> correctChoices,
                                           List<UserProblemChoice> userChoices,
                                           Integer correctRate) {
        return HistoryDetailResponse.builder()
                .problemId(userProblem.getProblem().getId())
                .problemType(userProblem.getProblem().getType())
                .answerStatus(userProblem.getAnswerStatus())
                .explanation(userProblem.getProblem().getExplanation())
                .correctChoiceIds(correctChoices.stream().map(Choice::getId).toList())
                .correctAnswer(userProblem.getProblem().getAnswer())
                .userChoiceIds(userChoices.stream().map(upc -> upc.getChoice().getId()).toList())
                .userSubjectiveAnswer(userProblem.getSubjectiveAnswer())
                .answerCorrectRate(correctRate)
                .build();
    }
}
