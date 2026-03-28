package com.problemsolving.api.history.service;

import com.problemsolving.api.common.exception.ResourceNotFoundException;
import com.problemsolving.api.history.dto.HistoryDetailResponse;
import com.problemsolving.api.history.dto.HistoryListResponse;
import com.problemsolving.api.redis.ProblemRedisService;
import com.problemsolving.core.domain.problem.entity.Choice;
import com.problemsolving.core.domain.userproblem.entity.UserProblem;
import com.problemsolving.core.domain.userproblem.entity.UserProblemChoice;
import com.problemsolving.core.repository.ChoiceRepository;
import com.problemsolving.core.repository.UserProblemChoiceRepository;
import com.problemsolving.core.repository.UserProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistoryService {

    private final UserProblemRepository userProblemRepository;
    private final UserProblemChoiceRepository userProblemChoiceRepository;
    private final ChoiceRepository choiceRepository;
    private final ProblemRedisService problemRedisService;

    /**
     * 특정 단원에서 사용자가 풀었던 문제 목록을 최신 제출순으로 반환한다.
     */
    public List<HistoryListResponse> getHistory(Long userId, Long chapterId) {
        return userProblemRepository.findSolvedHistoryByUserIdAndChapterId(userId, chapterId).stream()
                .map(HistoryListResponse::new)
                .toList();
    }

    public HistoryDetailResponse getHistoryDetail(Long userId, Long problemId) {
        UserProblem userProblem = userProblemRepository
                .findLatestSolvedByUserIdAndProblemId(userId, problemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "풀이 이력을 찾을 수 없습니다. userId=" + userId + ", problemId=" + problemId));

        List<Choice> correctChoices = choiceRepository.findByProblemIdAndIsCorrectTrue(problemId);
        List<UserProblemChoice> userChoices = userProblemChoiceRepository.findByUserProblemId(userProblem.getId());
        Integer correctRate = problemRedisService.getCorrectRate(problemId);

        return HistoryDetailResponse.of(userProblem, correctChoices, userChoices, correctRate);
    }
}
