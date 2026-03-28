package com.problemsolving.api.problem.service;

import com.problemsolving.api.common.exception.NoMoreProblemsException;
import com.problemsolving.api.common.exception.ResourceNotFoundException;
import com.problemsolving.api.problem.dto.RandomProblemRequest;
import com.problemsolving.api.problem.dto.RandomProblemResponse;
import com.problemsolving.api.redis.CorrectRateRedisService;
import com.problemsolving.core.domain.problem.entity.Choice;
import com.problemsolving.core.domain.problem.entity.Problem;
import com.problemsolving.core.repository.ChoiceRepository;
import com.problemsolving.core.repository.ProblemRepository;
import com.problemsolving.core.repository.UserProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final ChoiceRepository choiceRepository;
    private final UserProblemRepository userProblemRepository;
    private final CorrectRateRedisService correctRateRedisService;

    /**
     * 랜덤 문제 조회
     * 1. 챕터 전체 문제 ID 조회
     * 2. 이미 푼 문제 ID 제외
     * 3. 직전에 건너뛴 문제 ID 제외
     * 4. 남은 목록에서 랜덤 1개 반환
     */
    public RandomProblemResponse getRandomProblem(RandomProblemRequest request) {
        Long chapterId = request.getChapterId();
        Long userId = request.getUserId();

        // 챕터 전체 문제 ID
        List<Long> allProblemIds = new ArrayList<>(problemRepository.findIdsByChapterId(chapterId));

        // 이미 푼 문제 제외
        List<Long> solvedIds = userProblemRepository.findSolvedProblemIdsByUserId(userId);
        allProblemIds.removeAll(solvedIds);

        // 건너뛴 문제 처리: skipProblemId가 있으면 Redis에 저장 후 목록에서 제외
        Long skipProblemId = request.getSkipProblemId();
        if (skipProblemId != null) {
            correctRateRedisService.saveSkippedProblem(userId, chapterId, skipProblemId);
            allProblemIds.remove(skipProblemId);
        } else {
            // skipProblemId 없으면 직전에 건너뛴 문제 Redis에서 조회하여 제외
            Long lastSkippedId = correctRateRedisService.getLastSkippedProblemId(userId, chapterId);
            if (lastSkippedId != null) {
                allProblemIds.remove(lastSkippedId);
            }
        }

        if (allProblemIds.isEmpty()) {
            throw new NoMoreProblemsException();
        }

        // 랜덤 1개 선택
        int randomIndex = (int) (Math.random() * allProblemIds.size());
        Long selectedProblemId = allProblemIds.get(randomIndex);

        Problem problem = problemRepository.findById(selectedProblemId)
                .orElseThrow(() -> new ResourceNotFoundException("문제를 찾을 수 없습니다. id=" + selectedProblemId));

        List<Choice> choices = choiceRepository.findByProblemId(selectedProblemId);
        Integer correctRate = correctRateRedisService.getCorrectRate(selectedProblemId);

        return new RandomProblemResponse(problem, choices, correctRate);
    }
}
