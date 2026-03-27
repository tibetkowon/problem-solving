package com.problemsolving.api.chapter;

import com.problemsolving.api.chapter.dto.ChapterResponse;
import com.problemsolving.api.chapter.service.ChapterService;
import com.problemsolving.core.domain.chapter.entity.Chapter;
import com.problemsolving.core.repository.ChapterRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChapterService 단위 테스트")
class ChapterServiceTest {

    @Mock
    private ChapterRepository chapterRepository;

    @InjectMocks
    private ChapterService chapterService;

    @Test
    @DisplayName("단원 목록 조회 시 전체 단원을 반환한다")
    void 단원_목록조회_전체반환() {
        // Given
        Chapter ch1 = new Chapter("자료구조");
        Chapter ch2 = new Chapter("알고리즘");
        given(chapterRepository.findAll()).willReturn(List.of(ch1, ch2));

        // When
        List<ChapterResponse> result = chapterService.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("자료구조");
        assertThat(result.get(1).getName()).isEqualTo("알고리즘");
    }

    @Test
    @DisplayName("단원이 없을 때 빈 목록을 반환한다")
    void 단원_목록조회_없을때_빈목록반환() {
        // Given
        given(chapterRepository.findAll()).willReturn(List.of());

        // When
        List<ChapterResponse> result = chapterService.findAll();

        // Then
        assertThat(result).isEmpty();
    }
}
