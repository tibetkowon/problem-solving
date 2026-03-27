package com.problemsolving.api.problem;

import com.problemsolving.api.common.exception.NoMoreProblemsException;
import com.problemsolving.api.problem.dto.RandomProblemRequest;
import com.problemsolving.api.problem.dto.RandomProblemResponse;
import com.problemsolving.api.problem.service.ProblemService;
import com.problemsolving.api.redis.CorrectRateRedisService;
import com.problemsolving.core.constant.ProblemType;
import com.problemsolving.core.domain.chapter.entity.Chapter;
import com.problemsolving.core.domain.problem.entity.Choice;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProblemService 단위 테스트")
class ProblemServiceTest {

    @Mock private ProblemRepository problemRepository;
    @Mock private ChoiceRepository choiceRepository;
    @Mock private UserProblemRepository userProblemRepository;
    @Mock private CorrectRateRedisService correctRateRedisService;

    @InjectMocks
    private ProblemService problemService;

    private RandomProblemRequest request;
    private Chapter chapter;
    private Problem problem;

    @BeforeEach
    void setUp() {
        request = new RandomProblemRequest();
        // reflection으로 필드 설정 (또는 @Setter 추가 가능)
        setField(request, "chapterId", 1L);
        setField(request, "userId", 1L);

        chapter = new Chapter("자료구조");
        problem = Problem.builder()
                .chapter(chapter)
                .content("스택에 대한 설명으로 옳은 것은?")
                .type(ProblemType.OBJECTIVE_SINGLE)
                .explanation("스택은 LIFO 방식입니다.")
                .build();
        setField(problem, "id", 1L);
    }

    @Test
    @DisplayName("미풀이 문제가 있으면 랜덤으로 1개를 반환한다")
    void 랜덤문제조회_미풀이_1개반환() {
        // Given
        given(problemRepository.findIdsByChapterId(1L)).willReturn(List.of(1L, 2L, 3L));
        given(userProblemRepository.findSolvedProblemIdsByUserId(1L)).willReturn(List.of());
        given(correctRateRedisService.getLastSkippedProblemId(1L, 1L)).willReturn(null);
        given(problemRepository.findById(any())).willReturn(Optional.of(problem));
        given(choiceRepository.findByProblemId(any())).willReturn(List.of());
        given(correctRateRedisService.getCorrectRate(any())).willReturn(null);

        // When
        RandomProblemResponse response = problemService.getRandomProblem(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("스택에 대한 설명으로 옳은 것은?");
    }

    @Test
    @DisplayName("모든 문제를 풀었으면 NoMoreProblemsException이 발생한다")
    void 랜덤문제조회_모두풀면_예외발생() {
        // Given
        given(problemRepository.findIdsByChapterId(1L)).willReturn(List.of(1L, 2L));
        given(userProblemRepository.findSolvedProblemIdsByUserId(1L)).willReturn(List.of(1L, 2L));
        given(correctRateRedisService.getLastSkippedProblemId(1L, 1L)).willReturn(null);

        // When & Then
        assertThatThrownBy(() -> problemService.getRandomProblem(request))
                .isInstanceOf(NoMoreProblemsException.class);
    }

    @Test
    @DisplayName("직전에 건너뛴 문제는 랜덤 조회 대상에서 제외된다")
    void 랜덤문제조회_직전건너뜀_제외() {
        // Given: 문제 1, 2 중 문제 1이 직전에 건너뛰어졌고 문제 2가 미풀이
        given(problemRepository.findIdsByChapterId(1L)).willReturn(List.of(1L, 2L));
        given(userProblemRepository.findSolvedProblemIdsByUserId(1L)).willReturn(List.of());
        given(correctRateRedisService.getLastSkippedProblemId(1L, 1L)).willReturn(1L); // 1번 건너뜀

        Problem problem2 = Problem.builder()
                .chapter(chapter)
                .content("큐에 대한 설명은?")
                .type(ProblemType.OBJECTIVE_SINGLE)
                .explanation("큐는 FIFO 방식입니다.")
                .build();
        setField(problem2, "id", 2L);

        given(problemRepository.findById(2L)).willReturn(Optional.of(problem2));
        given(choiceRepository.findByProblemId(2L)).willReturn(List.of());
        given(correctRateRedisService.getCorrectRate(2L)).willReturn(null);

        // When
        RandomProblemResponse response = problemService.getRandomProblem(request);

        // Then: 반환된 문제는 반드시 2번
        assertThat(response.getContent()).isEqualTo("큐에 대한 설명은?");
    }

    @Test
    @DisplayName("정답률이 30명 이상이면 정수 정답률을 반환한다")
    void 랜덤문제조회_30명이상_정답률반환() {
        // Given
        given(problemRepository.findIdsByChapterId(1L)).willReturn(List.of(1L));
        given(userProblemRepository.findSolvedProblemIdsByUserId(1L)).willReturn(List.of());
        given(correctRateRedisService.getLastSkippedProblemId(1L, 1L)).willReturn(null);
        given(problemRepository.findById(1L)).willReturn(Optional.of(problem));
        given(choiceRepository.findByProblemId(1L)).willReturn(List.of());
        given(correctRateRedisService.getCorrectRate(1L)).willReturn(67);

        // When
        RandomProblemResponse response = problemService.getRandomProblem(request);

        // Then
        assertThat(response.getAnswerCorrectRate()).isEqualTo(67);
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
