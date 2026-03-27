package com.problemsolving.api.history.dto;

import com.problemsolving.core.constant.AnswerStatus;
import com.problemsolving.core.constant.ProblemType;
import com.problemsolving.core.domain.userproblem.entity.UserProblem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "풀었던 문제 목록 항목")
public class HistoryListResponse {

    @Schema(description = "문제 ID", example = "1")
    private final Long problemId;

    @Schema(description = "챕터 ID", example = "1")
    private final Long chapterId;

    @Schema(description = "문제 내용 요약")
    private final String content;

    @Schema(description = "문제 타입")
    private final ProblemType problemType;

    @Schema(description = "정답 여부")
    private final AnswerStatus answerStatus;

    @Schema(description = "제출 일시")
    private final LocalDateTime solvedAt;

    public HistoryListResponse(UserProblem userProblem) {
        this.problemId = userProblem.getProblem().getId();
        this.chapterId = userProblem.getProblem().getChapter().getId();
        this.content = userProblem.getProblem().getContent();
        this.problemType = userProblem.getProblem().getType();
        this.answerStatus = userProblem.getAnswerStatus();
        this.solvedAt = userProblem.getCreatedAt();
    }
}
