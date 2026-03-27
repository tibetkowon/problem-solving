package com.problemsolving.api.chapter.dto;

import com.problemsolving.core.domain.chapter.entity.Chapter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "단원 응답")
public class ChapterResponse {

    @Schema(description = "단원 ID", example = "1")
    private final Long id;

    @Schema(description = "단원명", example = "자료구조")
    private final String name;

    public ChapterResponse(Chapter chapter) {
        this.id = chapter.getId();
        this.name = chapter.getName();
    }
}
