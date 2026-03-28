package com.problemsolving.api.history;

import com.problemsolving.api.common.exception.ResourceNotFoundException;
import com.problemsolving.api.history.dto.HistoryListResponse;
import com.problemsolving.api.history.service.HistoryService;
import com.problemsolving.api.redis.ProblemRedisService;
import com.problemsolving.core.constant.AnswerStatus;
import com.problemsolving.core.constant.ProblemType;
import com.problemsolving.core.domain.chapter.entity.Chapter;
import com.problemsolving.core.domain.problem.entity.Problem;
import com.problemsolving.core.domain.userproblem.entity.UserProblem;
import com.problemsolving.core.repository.ChoiceRepository;
import com.problemsolving.core.repository.UserProblemChoiceRepository;
import com.problemsolving.core.repository.UserProblemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("HistoryService 단위 테스트")
class HistoryServiceTest {

    @Mock private UserProblemRepository userProblemRepository;
    @Mock private UserProblemChoiceRepository userProblemChoiceRepository;
    @Mock private ChoiceRepository choiceRepository;
    @Mock private ProblemRedisService problemRedisService;

    @InjectMocks
    private HistoryService historyService;

    private Chapter chapter1;
    private Problem problem1;
    private Problem problem2;

    @BeforeEach
    void setUp() {
        chapter1 = new Chapter("자료구조");
        setField(chapter1, "id", 1L);

        problem1 = Problem.builder()
                .chapter(chapter1).content("스택이란?")
                .type(ProblemType.OBJECTIVE_SINGLE).explanation("LIFO").build();
        problem2 = Problem.builder()
                .chapter(chapter1).content("큐란?")
                .type(ProblemType.SUBJECTIVE).explanation("FIFO").build();
        setField(problem1, "id", 1L);
        setField(problem2, "id", 2L);
    }

    @Test
    @DisplayName("chapterId로 필터링된 풀이 이력을 반환한다")
    void 풀이이력_단원필터링() {
        // Given
        UserProblem up1 = buildUserProblem(1L, problem1, AnswerStatus.CORRECT);
        UserProblem up2 = buildUserProblem(1L, problem2, AnswerStatus.WRONG);
        given(userProblemRepository.findSolvedHistoryByUserIdAndChapterId(1L, 1L))
                .willReturn(List.of(up1, up2));

        // When
        List<HistoryListResponse> result = historyService.getHistory(1L, 1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getProblemId()).isEqualTo(1L);
        assertThat(result.get(0).getAnswerStatus()).isEqualTo(AnswerStatus.CORRECT);
        assertThat(result.get(1).getProblemId()).isEqualTo(2L);
        assertThat(result.get(1).getAnswerStatus()).isEqualTo(AnswerStatus.WRONG);
    }

    @Test
    @DisplayName("해당 단원에 풀이 이력이 없으면 빈 목록을 반환한다")
    void 풀이이력_없으면_빈목록() {
        // Given
        given(userProblemRepository.findSolvedHistoryByUserIdAndChapterId(1L, 1L))
                .willReturn(List.of());

        // When
        List<HistoryListResponse> result = historyService.getHistory(1L, 1L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("풀이 이력 상세 조회 시 존재하지 않으면 ResourceNotFoundException이 발생한다")
    void 풀이이력_상세조회_없으면_예외발생() {
        // Given
        given(userProblemRepository.findLatestSolvedByUserIdAndProblemId(1L, 99L))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> historyService.getHistoryDetail(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private UserProblem buildUserProblem(Long userId, Problem problem, AnswerStatus status) {
        return UserProblem.builder()
                .userId(userId)
                .problem(problem)
                .answerStatus(status)
                .build();
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = findField(target.getClass(), fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("필드 설정 실패: " + fieldName, e);
        }
    }

    private java.lang.reflect.Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new RuntimeException("필드를 찾을 수 없음: " + fieldName);
    }
}
