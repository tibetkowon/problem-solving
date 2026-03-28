package com.problemsolving.api.problem.controller;

import com.problemsolving.api.problem.dto.RandomProblemRequest;
import com.problemsolving.api.problem.dto.RandomProblemResponse;
import com.problemsolving.api.problem.dto.SkipRequest;
import com.problemsolving.api.problem.service.ProblemService;
import com.problemsolving.api.problem.service.SkipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
@Tag(name = "Problem", description = "문제 관련 API")
public class ProblemController {

    private final ProblemService problemService;
    private final SkipService skipService;

    @PostMapping("/random")
    @Operation(summary = "랜덤 문제 조회",
            description = "사용자가 아직 풀지 않은 문제 중 랜덤으로 1개를 반환합니다. " +
                    "직전에 건너뛴 문제는 제외됩니다. 더 이상 제공할 문제가 없으면 204를 반환합니다.")
    public ResponseEntity<RandomProblemResponse> getRandomProblem(
            @Valid @RequestBody RandomProblemRequest request) {
        return ResponseEntity.ok(problemService.getRandomProblem(request));
    }

    @PostMapping("/skip")
    @Operation(summary = "문제 건너뛰기",
            description = "현재 문제를 풀지 않고 건너뜁니다. 건너뛴 문제는 바로 다음 랜덤 조회 시 제외됩니다.")
    public ResponseEntity<Void> skip(@Valid @RequestBody SkipRequest request) {
        skipService.skip(request.getUserId(), request.getProblemId(), request.getChapterId());
        return ResponseEntity.noContent().build();
    }
}
