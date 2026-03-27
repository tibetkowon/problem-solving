package com.problemsolving.api.chapter.controller;

import com.problemsolving.api.chapter.dto.ChapterResponse;
import com.problemsolving.api.chapter.service.ChapterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chapters")
@RequiredArgsConstructor
@Tag(name = "Chapter", description = "단원 관련 API")
public class ChapterController {

    private final ChapterService chapterService;

    @GetMapping
    @Operation(summary = "단원 목록 조회", description = "전체 단원 목록을 조회합니다.")
    public ResponseEntity<List<ChapterResponse>> getChapters() {
        return ResponseEntity.ok(chapterService.findAll());
    }
}
