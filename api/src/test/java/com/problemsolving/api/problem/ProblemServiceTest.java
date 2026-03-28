package com.problemsolving.api.problem;

import com.problemsolving.api.common.exception.NoAvailableProblemsException;
import com.problemsolving.api.common.exception.NoMoreProblemsException;
import com.problemsolving.api.common.exception.NoProblemInChapterException;
import com.problemsolving.api.problem.dto.RandomProblemRequest;
import com.problemsolving.api.problem.dto.RandomProblemResponse;
import com.problemsolving.api.problem.service.ProblemService;
import com.problemsolving.api.redis.ProblemRedisService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProblemService 단위 테스트")
class ProblemServiceTest {

    @Mock private ProblemRepository problemRepository;
    @Mock private ChoiceRepository choiceRepository;
    @Mock private UserProblemRepository userProblemRepository;
    @Mock private ProblemRedisService problemRedisService;

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
        // Given: 전체 3문제, 풀이 0, Redis 이력 없음
        given(problemRepository.findIdsByChapterId(1L)).willReturn(List.of(1L, 2L, 3L));
        given(userProblemRepository.findSolvedProblemIdsByUserId(1L)).willReturn(List.of());
        given(problemRedisService.getCurrentProblemId(1L, 1L)).willReturn(null);
        given(problemRedisService.getLastSkippedProblemId(1L, 1L)).willReturn(null);
        given(problemRepository.findById(any())).willReturn(Optional.of(problem1));
        given(choiceRepository.findByProblemId(any())).willReturn(List.of());
        given(problemRedisService.getCorrectRate(any())).willReturn(null);

        // When
        RandomProblemResponse response = problemService.getRandomProblem(buildRequest(1L, 1L));

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTotalCount()).isEqualTo(3);
        assertThat(response.getSolvedCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("solvedCount는 해당 챕터 내 풀이 수만 반영된다 (다른 챕터 풀이 제외)")
    void 랜덤문제조회_solvedCount_챕터내풀이만_반영() {
        // Given: 챕터1 문제 [1,2,3], 풀이 완료 = [1(챕터1), 99(다른챕터)] → solvedCount = 1
        given(problemRepository.findIdsByChapterId(1L)).willReturn(List.of(1L, 2L, 3L));
        given(userProblemRepository.findSolvedProblemIdsByUserId(1L)).willReturn(List.of(1L, 99L));
        given(problemRedisService.getCurrentProblemId(1L, 1L)).willReturn(null);
        given(problemRedisService.getLastSkippedProblemId(1L, 1L)).willReturn(null);
        given(problemRepository.findById(any())).willReturn(Optional.of(problem2));
        given(choiceRepository.findByProblemId(any())).willReturn(List.of());
        given(problemRedisService.getCorrectRate(any())).willReturn(null);

        // When
        RandomProblemResponse response = problemService.getRandomProblem(buildRequest(1L, 1L));

        // Then: 챕터1의 totalCount=3, 챕터1 내 solvedCount=1 (99번은 다른 챕터이므로 제외)
        assertThat(response.getTotalCount()).isEqualTo(3);
        assertThat(response.getSolvedCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("단원에 문제가 0개이면 NoProblemInChapterException이 발생한다")
    void 랜덤문제조회_단원문제없음_예외발생() {
        // Given
        given(problemRepository.findIdsByChapterId(1L)).willReturn(List.of());

        // When & Then
        assertThatThrownBy(() -> problemService.getRandomProblem(buildRequest(1L, 1L)))
                .isInstanceOf(NoProblemInChapterException.class);
    }

    @Test
    @DisplayName("모든 문제를 풀었으면 NoMoreProblemsException이 발생한다")
    void 랜덤문제조회_모두풀면_예외발생() {
        // Given
        given(problemRepository.findIdsByChapterId(1L)).willReturn(List.of(1L, 2L));
        given(userProblemRepository.findSolvedProblemIdsByUserId(1L)).willReturn(List.of(1L, 2L));

        // When & Then
        assertThatThrownBy(() -> problemService.getRandomProblem(buildRequest(1L, 1L)))
                .isInstanceOf(NoMoreProblemsException.class);
    }

    @Test
    @DisplayName("현재 노출 중인 문제는 제외되고 Redis에 lastSkipped로 이동된다")
    void 랜덤문제조회_현재노출문제_제외_및_lastSkipped_저장() {
        // Given: 문제 1, 2, 3 / 현재 노출 중인 문제 = 1 → 2 또는 3이 반환되어야 함
        given(problemRepository.findIdsByChapterId(1L)).willReturn(List.of(1L, 2L, 3L));
        given(userProblemRepository.findSolvedProblemIdsByUserId(1L)).willReturn(List.of());
        given(problemRedisService.getCurrentProblemId(1L, 1L)).willReturn(1L);
        given(problemRedisService.getLastSkippedProblemId(1L, 1L)).willReturn(null);
        given(problemRepository.findById(any())).willReturn(Optional.of(problem2));
        given(choiceRepository.findByProblemId(any())).willReturn(List.of());
        given(problemRedisService.getCorrectRate(any())).willReturn(null);

        // When
        problemService.getRandomProblem(buildRequest(1L, 1L));

        // Then: 이전 현재 문제(1)가 lastSkipped로 저장되는지 확인
        verify(problemRedisService).saveLastSkippedProblem(1L, 1L, 1L);
        // 새로 선택된 문제가 current로 저장되는지 확인
        verify(problemRedisService).saveCurrentProblem(eq(1L), eq(1L), any());
    }

    @Test
    @DisplayName("현재 노출 문제와 직전 건너뛴 문제 둘 다 제외된다")
    void 랜덤문제조회_현재노출문제와_lastSkipped_둘다_제외() {
        // Given: 문제 1, 2, 3 / prevCurrent=2, lastSkipped=1 → 반드시 3만 나와야 함
        given(problemRepository.findIdsByChapterId(1L)).willReturn(List.of(1L, 2L, 3L));
        given(userProblemRepository.findSolvedProblemIdsByUserId(1L)).willReturn(List.of());
        given(problemRedisService.getCurrentProblemId(1L, 1L)).willReturn(2L);
        given(problemRedisService.getLastSkippedProblemId(1L, 1L)).willReturn(1L);
        given(problemRepository.findById(3L)).willReturn(Optional.of(problem3));
        given(choiceRepository.findByProblemId(3L)).willReturn(List.of());
        given(problemRedisService.getCorrectRate(3L)).willReturn(null);

        // When
        RandomProblemResponse response = problemService.getRandomProblem(buildRequest(1L, 1L));

        // Then: 1(lastSkipped), 2(prevCurrent) 모두 제외 → 3번만 가능
        assertThat(response.getContent()).isEqualTo("덱에 대한 설명은?");
    }

    @Test
    @DisplayName("lastSkipped 제외로 후보가 없으면 lastSkipped를 해제하고 재시도한다 (fallback)")
    void 랜덤문제조회_lastSkipped_제외_후보없음_fallback() {
        // Given: 문제 1, 2 / prevCurrent=2, lastSkipped=1 → 둘 다 제외 시 후보 없음
        //        → fallback: lastSkipped(1) 해제, prevCurrent(2)만 제외 → 1번 반환
        given(problemRepository.findIdsByChapterId(1L)).willReturn(List.of(1L, 2L));
        given(userProblemRepository.findSolvedProblemIdsByUserId(1L)).willReturn(List.of());
        given(problemRedisService.getCurrentProblemId(1L, 1L)).willReturn(2L);
        given(problemRedisService.getLastSkippedProblemId(1L, 1L)).willReturn(1L);
        given(problemRepository.findById(1L)).willReturn(Optional.of(problem1));
        given(choiceRepository.findByProblemId(1L)).willReturn(List.of());
        given(problemRedisService.getCorrectRate(1L)).willReturn(null);

        // When
        RandomProblemResponse response = problemService.getRandomProblem(buildRequest(1L, 1L));

        // Then: fallback으로 1번이 반환됨
        assertThat(response.getContent()).isEqualTo("스택에 대한 설명은?");
    }

    @Test
    @DisplayName("미풀이 문제가 1개이고 현재 노출 중이면 NoAvailableProblemsException이 발생한다")
    void 랜덤문제조회_미풀이1개_현재노출중_예외발생() {
        // Given: 전체 2문제, 1번 풀이 완료, 2번이 현재 노출 중 → 남은 미풀이 = [2], prevCurrent = 2 → 후보 없음
        given(problemRepository.findIdsByChapterId(1L)).willReturn(List.of(1L, 2L));
        given(userProblemRepository.findSolvedProblemIdsByUserId(1L)).willReturn(List.of(1L));
        given(problemRedisService.getCurrentProblemId(1L, 1L)).willReturn(2L);
        given(problemRedisService.getLastSkippedProblemId(1L, 1L)).willReturn(null);

        // When & Then: 유일한 미풀이 문제가 현재 노출 중 → 넘길 수 있는 새 문제 없음
        assertThatThrownBy(() -> problemService.getRandomProblem(buildRequest(1L, 1L)))
                .isInstanceOf(NoAvailableProblemsException.class);
    }

    @Test
    @DisplayName("정답률이 30명 이상이면 정수 정답률을 반환한다")
    void 랜덤문제조회_30명이상_정답률반환() {
        // Given
        given(problemRepository.findIdsByChapterId(1L)).willReturn(List.of(1L));
        given(userProblemRepository.findSolvedProblemIdsByUserId(1L)).willReturn(List.of());
        given(problemRedisService.getCurrentProblemId(1L, 1L)).willReturn(null);
        given(problemRedisService.getLastSkippedProblemId(1L, 1L)).willReturn(null);
        given(problemRepository.findById(1L)).willReturn(Optional.of(problem1));
        given(choiceRepository.findByProblemId(1L)).willReturn(List.of());
        given(problemRedisService.getCorrectRate(1L)).willReturn(67);

        // When
        RandomProblemResponse response = problemService.getRandomProblem(buildRequest(1L, 1L));

        // Then
        assertThat(response.getAnswerCorrectRate()).isEqualTo(67);
    }

    private RandomProblemRequest buildRequest(Long chapterId, Long userId) {
        RandomProblemRequest request = new RandomProblemRequest();
        setField(request, "chapterId", chapterId);
        setField(request, "userId", userId);
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
