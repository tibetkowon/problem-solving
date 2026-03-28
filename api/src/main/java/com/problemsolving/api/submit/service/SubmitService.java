package com.problemsolving.api.submit.service;

import com.problemsolving.api.common.exception.ResourceNotFoundException;
import com.problemsolving.api.redis.ProblemRedisService;
import com.problemsolving.api.submit.dto.SubmitRequest;
import com.problemsolving.api.submit.dto.SubmitResponse;
import com.problemsolving.core.constant.AnswerStatus;
import com.problemsolving.core.constant.ProblemType;
import com.problemsolving.core.domain.problem.entity.Choice;
import com.problemsolving.core.domain.problem.entity.Problem;
import com.problemsolving.core.domain.userproblem.entity.UserProblem;
import com.problemsolving.core.domain.userproblem.entity.UserProblemChoice;
import com.problemsolving.core.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubmitService {

    private final ProblemRepository problemRepository;
    private final ChoiceRepository choiceRepository;
    private final UserProblemRepository userProblemRepository;
    private final UserProblemChoiceRepository userProblemChoiceRepository;
    private final ProblemRedisService problemRedisService;

    @Transactional
    public SubmitResponse submit(SubmitRequest request) {
        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new ResourceNotFoundException("문제를 찾을 수 없습니다. id=" + request.getProblemId()));

        AnswerStatus answerStatus;
        SubmitResponse response;

        if (request.getAnswerType() == ProblemType.SUBJECTIVE) {
            answerStatus = judgeSubjective(problem, request.getSubjectiveAnswer());
            response = SubmitResponse.ofSubjective(
                    problem.getId(), answerStatus, problem.getExplanation(), problem.getAnswer());
            saveUserProblem(request.getUserId(), problem, request.getSubjectiveAnswer(), answerStatus);
        } else {
            List<Choice> correctChoices = choiceRepository.findByProblemIdAndIsCorrectTrue(problem.getId());
            answerStatus = judgeObjective(correctChoices, request.getSelectedChoiceIds());
            response = SubmitResponse.ofObjective(problem.getId(), answerStatus, problem.getExplanation(), correctChoices);
            UserProblem userProblem = saveUserProblem(request.getUserId(), problem, null, answerStatus);
            saveUserProblemChoices(userProblem, request.getSelectedChoiceIds());
        }

        // 정답률은 CORRECT인 경우만 정답으로 카운트 (부분 정답은 오답으로 처리)
        boolean isCorrectForRate = answerStatus == AnswerStatus.CORRECT;
        problemRedisService.incrementCorrectRate(problem.getId(), isCorrectForRate);

        return response;
    }

    /**
     * 주관식 정답 판정: 정확 일치
     */
    private AnswerStatus judgeSubjective(Problem problem, String userAnswer) {
        if (userAnswer == null || userAnswer.isBlank()) {
            return AnswerStatus.WRONG;
        }
        if (problem.getAnswer() == null) {
            return AnswerStatus.WRONG;
        }
        return problem.getAnswer().trim().equals(userAnswer.trim())
                ? AnswerStatus.CORRECT
                : AnswerStatus.WRONG;
    }

    /**
     * 객관식 정답 판정
     * - 정답 선택지와 완전히 일치 → CORRECT
     * - 정답 선택지 중 1개 이상 포함 (오답 포함 여부 무관) → PARTIAL
     * - 교집합 없음 → WRONG
     */
    private AnswerStatus judgeObjective(List<Choice> correctChoices, List<Long> selectedIds) {
        if (selectedIds == null || selectedIds.isEmpty()) {
            return AnswerStatus.WRONG;
        }

        Set<Long> correctIds = correctChoices.stream().map(Choice::getId).collect(Collectors.toSet());
        Set<Long> selectedSet = Set.copyOf(selectedIds);

        boolean hasIntersection = selectedSet.stream().anyMatch(correctIds::contains);
        boolean isExactMatch = correctIds.equals(selectedSet);

        if (isExactMatch) return AnswerStatus.CORRECT;
        if (hasIntersection) return AnswerStatus.PARTIAL;
        return AnswerStatus.WRONG;
    }

    private UserProblem saveUserProblem(Long userId, Problem problem, String subjectiveAnswer, AnswerStatus status) {
        UserProblem userProblem = UserProblem.builder()
                .userId(userId)
                .problem(problem)
                .subjectiveAnswer(subjectiveAnswer)
                .answerStatus(status)
                .build();
        return userProblemRepository.save(userProblem);
    }

    private void saveUserProblemChoices(UserProblem userProblem, List<Long> selectedChoiceIds) {
        if (selectedChoiceIds == null) return;
        selectedChoiceIds.forEach(choiceId ->
                choiceRepository.findById(choiceId).ifPresent(choice -> {
                    UserProblemChoice upc = UserProblemChoice.builder()
                            .userProblem(userProblem)
                            .choice(choice)
                            .build();
                    userProblemChoiceRepository.save(upc);
                })
        );
    }
}
