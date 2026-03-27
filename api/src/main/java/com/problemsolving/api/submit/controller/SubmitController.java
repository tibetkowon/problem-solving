package com.problemsolving.api.submit.controller;

import com.problemsolving.api.submit.dto.SubmitRequest;
import com.problemsolving.api.submit.dto.SubmitResponse;
import com.problemsolving.api.submit.service.SubmitService;
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
public class SubmitController {

    private final SubmitService submitService;

    @PostMapping("/submit")
    @Operation(summary = "문제 제출",
            description = "객관식/주관식 답안을 제출합니다. " +
                    "제출 즉시 정답 여부(정답/부분정답/오답)와 해설이 반환됩니다.")
    public ResponseEntity<SubmitResponse> submit(@Valid @RequestBody SubmitRequest request) {
        return ResponseEntity.ok(submitService.submit(request));
    }
}
