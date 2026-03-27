package com.problemsolving.api.submit;

import com.problemsolving.api.redis.CorrectRateRedisService;
import com.problemsolving.api.submit.dto.SubmitRequest;
import com.problemsolving.api.submit.dto.SubmitResponse;
import com.problemsolving.api.submit.service.SubmitService;
import com.problemsolving.core.constant.AnswerStatus;
import com.problemsolving.core.constant.ProblemType;
import com.problemsolving.core.domain.chapter.entity.Chapter;
import com.problemsolving.core.domain.problem.entity.Choice;
import com.problemsolving.core.domain.problem.entity.Problem;
import com.problemsolving.core.domain.userproblem.entity.UserProblem;
import com.problemsolving.core.repository.*;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubmitService 단위 테스트")
class SubmitServiceTest {

    @Mock private ProblemRepository problemRepository;
    @Mock private ChoiceRepository choiceRepository;
    @Mock private UserProblemRepository userProblemRepository;
    @Mock private UserProblemChoiceRepository userProblemChoiceRepository;
    @Mock private CorrectRateRedisService correctRateRedisService;

    @InjectMocks
    private SubmitService submitService;

    private Problem objectiveSingleProblem;
    private Problem objectiveMultipleProblem;
    private Problem subjectiveProblem;
    private Choice correctChoice1;
    private Choice correctChoice2;
    private Choice wrongChoice;

    @BeforeEach
    void setUp() {
        Chapter chapter = new Chapter("자료구조");

        objectiveSingleProblem = Problem.builder()
                .chapter(chapter).content("스택이란?")
                .type(ProblemType.OBJECTIVE_SINGLE).explanation("스택은 LIFO").build();
        setId(objectiveSingleProblem, 1L);

        objectiveMultipleProblem = Problem.builder()
                .chapter(chapter).content("선형 자료구조는?")
                .type(ProblemType.OBJECTIVE_MULTIPLE).explanation("배열, 스택, 큐").build();
        setId(objectiveMultipleProblem, 2L);

        subjectiveProblem = Problem.builder()
                .chapter(chapter).content("FIFO 자료구조는?")
                .type(ProblemType.SUBJECTIVE).explanation("큐").answer("큐").build();
        setId(subjectiveProblem, 3L);

        correctChoice1 = Choice.builder().problem(objectiveMultipleProblem).content("배열").isCorrect(true).build();
        correctChoice2 = Choice.builder().problem(objectiveMultipleProblem).content("스택").isCorrect(true).build();
        wrongChoice = Choice.builder().problem(objectiveMultipleProblem).content("트리").isCorrect(false).build();
        setId(correctChoice1, 1L);
        setId(correctChoice2, 2L);
        setId(wrongChoice, 3L);

        given(userProblemRepository.save(any())).willReturn(UserProblem.builder()
                .userId(1L).problem(objectiveSingleProblem).answerStatus(AnswerStatus.CORRECT).build());
    }

    @Test
    @DisplayName("객관식 단일 정답 완전 일치 시 CORRECT를 반환한다")
    void 객관식_단일정답_완전일치_CORRECT() {
        // Given
        Choice correct = Choice.builder().problem(objectiveSingleProblem).content("스택").isCorrect(true).build();
        setId(correct, 10L);
        given(problemRepository.findById(1L)).willReturn(Optional.of(objectiveSingleProblem));
        given(choiceRepository.findByProblemIdAndIsCorrectTrue(1L)).willReturn(List.of(correct));

        SubmitRequest request = buildRequest(1L, ProblemType.OBJECTIVE_SINGLE, List.of(10L), null);

        // When
        SubmitResponse response = submitService.submit(request);

        // Then
        assertThat(response.getAnswerStatus()).isEqualTo(AnswerStatus.CORRECT);
    }

    @Test
    @DisplayName("객관식 복수 정답 중 일부만 맞추면 PARTIAL을 반환한다")
    void 객관식_복수정답_일부일치_PARTIAL() {
        // Given: 정답은 [1, 2], 사용자는 [1, 3] 제출
        given(problemRepository.findById(2L)).willReturn(Optional.of(objectiveMultipleProblem));
        given(choiceRepository.findByProblemIdAndIsCorrectTrue(2L)).willReturn(List.of(correctChoice1, correctChoice2));

        SubmitRequest request = buildRequest(2L, ProblemType.OBJECTIVE_MULTIPLE, List.of(1L, 3L), null);

        // When
        SubmitResponse response = submitService.submit(request);

        // Then
        assertThat(response.getAnswerStatus()).isEqualTo(AnswerStatus.PARTIAL);
    }

    @Test
    @DisplayName("객관식에서 정답 선택지를 하나도 선택하지 않으면 WRONG을 반환한다")
    void 객관식_정답없음_WRONG() {
        // Given
        given(problemRepository.findById(2L)).willReturn(Optional.of(objectiveMultipleProblem));
        given(choiceRepository.findByProblemIdAndIsCorrectTrue(2L)).willReturn(List.of(correctChoice1, correctChoice2));

        SubmitRequest request = buildRequest(2L, ProblemType.OBJECTIVE_MULTIPLE, List.of(3L), null);

        // When
        SubmitResponse response = submitService.submit(request);

        // Then
        assertThat(response.getAnswerStatus()).isEqualTo(AnswerStatus.WRONG);
    }

    @Test
    @DisplayName("주관식 정답이 정확히 일치하면 CORRECT를 반환한다")
    void 주관식_정확일치_CORRECT() {
        // Given
        given(problemRepository.findById(3L)).willReturn(Optional.of(subjectiveProblem));
        SubmitRequest request = buildRequest(3L, ProblemType.SUBJECTIVE, null, "큐");

        // When
        SubmitResponse response = submitService.submit(request);

        // Then
        assertThat(response.getAnswerStatus()).isEqualTo(AnswerStatus.CORRECT);
    }

    @Test
    @DisplayName("주관식 답변이 다르면 WRONG을 반환한다")
    void 주관식_불일치_WRONG() {
        // Given
        given(problemRepository.findById(3L)).willReturn(Optional.of(subjectiveProblem));
        SubmitRequest request = buildRequest(3L, ProblemType.SUBJECTIVE, null, "스택");

        // When
        SubmitResponse response = submitService.submit(request);

        // Then
        assertThat(response.getAnswerStatus()).isEqualTo(AnswerStatus.WRONG);
    }

    private SubmitRequest buildRequest(Long problemId, ProblemType type, List<Long> choiceIds, String subjective) {
        SubmitRequest req = new SubmitRequest();
        setFieldValue(req, "problemId", problemId);
        setFieldValue(req, "userId", 1L);
        setFieldValue(req, "answerType", type);
        setFieldValue(req, "selectedChoiceIds", choiceIds);
        setFieldValue(req, "subjectiveAnswer", subjective);
        return req;
    }

    private void setId(Object target, Long id) {
        setFieldValue(target, "id", id);
    }

    private void setFieldValue(Object target, String fieldName, Object value) {
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
            try { return current.getDeclaredField(fieldName); }
            catch (NoSuchFieldException e) { current = current.getSuperclass(); }
        }
        throw new RuntimeException("필드를 찾을 수 없음: " + fieldName);
    }
}
