package com.problemsolving.api.history.controller;

import com.problemsolving.api.history.dto.HistoryDetailResponse;
import com.problemsolving.api.history.dto.HistoryListResponse;
import com.problemsolving.api.history.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/users/{userId}/problems")
@RequiredArgsConstructor
@Tag(name = "History", description = "풀이 이력 관련 API")
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    @Operation(summary = "풀이 이력 목록 조회",
            description = "특정 단원에서 사용자가 풀었던 문제 목록을 최신 제출순으로 반환합니다.")
    public ResponseEntity<List<HistoryListResponse>> getHistory(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long userId,
            @Parameter(description = "단원 ID", example = "1", required = true)
            @NotNull @RequestParam Long chapterId) {
        return ResponseEntity.ok(historyService.getHistory(userId, chapterId));
    }

    @GetMapping("/{problemId}")
    @Operation(summary = "풀이 이력 상세 조회",
            description = "특정 문제의 풀이 결과 상세 정보를 반환합니다. 정답, 해설, 사용자 답변, 정답률을 포함합니다.")
    public ResponseEntity<HistoryDetailResponse> getHistoryDetail(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long userId,
            @Parameter(description = "문제 ID", example = "1")
            @PathVariable Long problemId) {
        return ResponseEntity.ok(historyService.getHistoryDetail(userId, problemId));
    }
}
