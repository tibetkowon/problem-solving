package com.problemsolving.api.problem;

import com.problemsolving.api.common.exception.NoMoreProblemsException;
import com.problemsolving.api.problem.dto.RandomProblemRequest;
import com.problemsolving.api.problem.dto.RandomProblemResponse;
import com.problemsolving.api.problem.service.ProblemService;
import com.problemsolving.api.redis.CorrectRateRedisService;
import com.problemsolving.core.constant.ProblemType;
import com.problemsolving.core.domain.chapter.entity.Chapter;
import com.problemsolving.core.domain.problem.entity.Problem;
import com.problemsolving.core.repository.ChoiceRepository;
import com.problemsolving.core.repository.ProblemRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProblemService 단위 테스트")
class ProblemServiceTest {

    @Mock private ProblemRepository problemRepository;
    @Mock private ChoiceRepository choiceRepository;
    @Mock private UserProblemRepository userProblemRepository;
    @Mock private CorrectRateRedisService correctRateRedisService;

    @InjectMocks
    private ProblemService problemService;

    private Chapter chapter;
    private Problem problem1;
    private Problem problem2;
    private Problem problem3;

    @BeforeEach
    void setUp() {
        chapter = new Chapter("자료구조");

        problem1 = Problem.builder()
                .chapter(chapter).content("스택에 대한 설명은?")
                .type(ProblemType.OBJECTIVE_SINGLE).explanation("LIFO").build();
        setField(problem1, "id", 1L);

        problem2 = Problem.builder()
                .chapter(chapter).content("큐에 대한 설명은?")
                .type(ProblemType.OBJECTIVE_SINGLE).explanation("FIFO").build();
        setField(problem2, "id", 2L);

        problem3 = Problem.builder()
                .chapter(chapter).content("덱에 대한 설명은?")
                .type(ProblemType.OBJECTIVE_SINGLE).explanation("양방향").build();
        setField(problem3, "id", 3L);
    }

    @Test
    @DisplayName("미풀이 문제가 있으면 랜덤으로 1개를 반환한다")
    void 랜덤문제조회_미풀이_1개반환() {
        // Given
        given(problemRepository.findIdsByChapterId(1L)).willReturn(List.of(1L, 2L, 3L));
        given(userProblemRepository.findSolvedProblemIdsByUserId(1L)).willReturn(List.of());
        given(correctRateRedisService.getLastSkippedProblemId(1L, 1L)).willReturn(null);
        given(problemRepository.findById(any())).willReturn(Optional.of(problem1));
        given(choiceRepository.findByProblemId(any())).willReturn(List.of());
        given(correctRateRedisService.getCorrectRate(any())).willReturn(null);

        // When
        RandomProblemResponse response = problemService.getRandomProblem(buildRequest(1L, 1L, null));

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTotalCount()).isEqualTo(3);
        assertThat(response.getSolvedCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("totalCount와 solvedCount가 응답에 정확히 반영된다")
    void 랜덤문제조회_totalCount_solvedCount_정확히반환() {
        // Given: 전체 3문제, 1문제 풀이 완료
        given(problemRepository.findIdsByChapterId(1L)).willReturn(List.of(1L, 2L, 3L));
        given(userProblemRepository.findSolvedProblemIdsByUserId(1L)).willReturn(List.of(1L));
        given(correctRateRedisService.getLastSkippedProblemId(1L, 1L)).willReturn(null);
        given(problemRepository.findById(any())).willReturn(Optional.of(problem2));
        given(choiceRepository.findByProblemId(any())).willReturn(List.of());
        given(correctRateRedisService.getCorrectRate(any())).willReturn(null);

        // When
        RandomProblemResponse response = problemService.getRandomProblem(buildRequest(1L, 1L, null));

        // Then
        assertThat(response.getTotalCount()).isEqualTo(3);
        assertThat(response.getSolvedCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("모든 문제를 풀었으면 NoMoreProblemsException이 발생한다")
    void 랜덤문제조회_모두풀면_예외발생() {
        // Given
        given(problemRepository.findIdsByChapterId(1L)).willReturn(List.of(1L, 2L));
        given(userProblemRepository.findSolvedProblemIdsByUserId(1L)).willReturn(List.of(1L, 2L));
        given(correctRateRedisService.getLastSkippedProblemId(1L, 1L)).willReturn(null);

        // When & Then
        assertThatThrownBy(() -> problemService.getRandomProblem(buildRequest(1L, 1L, null)))
                .isInstanceOf(NoMoreProblemsException.class);
    }

    @Test
    @DisplayName("skipProblemId 전달 시 Redis에 저장하고 해당 문제를 제외한다")
    void 랜덤문제조회_skipProblemId_Redis저장_및_제외() {
        // Given: 문제 1, 2, 3 중 1번을 스킵 → 2 또는 3이 반환되어야 함
        given(problemRepository.findIdsByChapterId(1L)).willReturn(List.of(1L, 2L, 3L));
        given(userProblemRepository.findSolvedProblemIdsByUserId(1L)).willReturn(List.of());
        given(correctRateRedisService.getLastSkippedProblemId(1L, 1L)).willReturn(null);
        given(problemRepository.findById(any())).willReturn(Optional.of(problem2));
        given(choiceRepository.findByProblemId(any())).willReturn(List.of());
        given(correctRateRedisService.getCorrectRate(any())).willReturn(null);

        // When
        problemService.getRandomProblem(buildRequest(1L, 1L, 1L));

        // Then: Redis에 스킵 저장 호출 확인
        verify(correctRateRedisService).saveSkippedProblem(1L, 1L, 1L);
    }

    @Test
    @DisplayName("skipProblemId와 직전 건너뛴 문제 둘 다 제외된다")
    void 랜덤문제조회_skipProblemId와_직전스킵_둘다_제외() {
        // Given: 문제 1, 2, 3 / 직전 스킵=1 / 현재 스킵=2 → 반드시 3만 나와야 함
        given(problemRepository.findIdsByChapterId(1L)).willReturn(List.of(1L, 2L, 3L));
        given(userProblemRepository.findSolvedProblemIdsByUserId(1L)).willReturn(List.of());
        given(correctRateRedisService.getLastSkippedProblemId(1L, 1L)).willReturn(1L);
        given(problemRepository.findById(3L)).willReturn(Optional.of(problem3));
        given(choiceRepository.findByProblemId(3L)).willReturn(List.of());
        given(correctRateRedisService.getCorrectRate(3L)).willReturn(null);

        // When
        RandomProblemResponse response = problemService.getRandomProblem(buildRequest(1L, 1L, 2L));

        // Then: 1(직전 스킵), 2(현재 스킵) 모두 제외 → 3번만 가능
        assertThat(response.getContent()).isEqualTo("덱에 대한 설명은?");
    }

    @Test
    @DisplayName("스킵 후 남은 문제가 없으면 NoMoreProblemsException이 발생한다")
    void 랜덤문제조회_스킵후_남은문제없음_예외발생() {
        // Given: 문제 1, 2 / 직전 스킵=1 / 현재 스킵=2 → 남은 문제 없음
        given(problemRepository.findIdsByChapterId(1L)).willReturn(List.of(1L, 2L));
        given(userProblemRepository.findSolvedProblemIdsByUserId(1L)).willReturn(List.of());
        given(correctRateRedisService.getLastSkippedProblemId(1L, 1L)).willReturn(1L);

        // When & Then
        assertThatThrownBy(() -> problemService.getRandomProblem(buildRequest(1L, 1L, 2L)))
                .isInstanceOf(NoMoreProblemsException.class);
    }

    @Test
    @DisplayName("정답률이 30명 이상이면 정수 정답률을 반환한다")
    void 랜덤문제조회_30명이상_정답률반환() {
        // Given
        given(problemRepository.findIdsByChapterId(1L)).willReturn(List.of(1L));
        given(userProblemRepository.findSolvedProblemIdsByUserId(1L)).willReturn(List.of());
        given(correctRateRedisService.getLastSkippedProblemId(1L, 1L)).willReturn(null);
        given(problemRepository.findById(1L)).willReturn(Optional.of(problem1));
        given(choiceRepository.findByProblemId(1L)).willReturn(List.of());
        given(correctRateRedisService.getCorrectRate(1L)).willReturn(67);

        // When
        RandomProblemResponse response = problemService.getRandomProblem(buildRequest(1L, 1L, null));

        // Then
        assertThat(response.getAnswerCorrectRate()).isEqualTo(67);
    }

    private RandomProblemRequest buildRequest(Long chapterId, Long userId, Long skipProblemId) {
        RandomProblemRequest request = new RandomProblemRequest();
        setField(request, "chapterId", chapterId);
        setField(request, "userId", userId);
        setField(request, "skipProblemId", skipProblemId);
        return request;
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
