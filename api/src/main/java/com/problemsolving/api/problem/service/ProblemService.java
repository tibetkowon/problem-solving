package com.problemsolving.api.problem.service;

import com.problemsolving.api.common.exception.NoAvailableProblemsException;
import com.problemsolving.api.common.exception.NoMoreProblemsException;
import com.problemsolving.api.common.exception.NoProblemInChapterException;
import com.problemsolving.api.common.exception.ResourceNotFoundException;
import com.problemsolving.api.problem.dto.RandomProblemRequest;
import com.problemsolving.api.problem.dto.RandomProblemResponse;
import com.problemsolving.api.redis.ProblemRedisService;
import com.problemsolving.core.domain.problem.entity.Choice;
import com.problemsolving.core.domain.problem.entity.Problem;
import com.problemsolving.core.repository.ChoiceRepository;
import com.problemsolving.core.repository.ProblemRepository;
import com.problemsolving.core.repository.UserProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final ChoiceRepository choiceRepository;
    private final UserProblemRepository userProblemRepository;
    private final ProblemRedisService problemRedisService;

    /**
     * 랜덤 문제 조회
     *
     * <p>제외 순서 (우선순위 높음 → 낮음):
     * <ol>
     *   <li>이미 풀이 완료한 문제</li>
     *   <li>현재 노출 중인 문제 (prevCurrentId) — 항상 제외</li>
     *   <li>직전 건너뛴 문제 (lastSkippedId) — 후보가 없으면 제외 해제</li>
     * </ol>
     *
     * <p>fallback: prevCurrentId + lastSkippedId 모두 제외 후 후보가 없으면
     * lastSkippedId 제외를 해제하고 재시도. 여전히 없으면 {@link NoMoreProblemsException}.
     */
    public RandomProblemResponse getRandomProblem(RandomProblemRequest request) {
        Long chapterId = request.getChapterId();
        Long userId = request.getUserId();

        // 챕터 전체 문제 ID
        List<Long> allProblemIds = new ArrayList<>(problemRepository.findIdsByChapterId(chapterId));
        int totalCount = allProblemIds.size();

        // 케이스 1: 단원에 문제가 0개
        if (totalCount == 0) {
            throw new NoProblemInChapterException();
        }

        // 이미 푼 문제 제외
        List<Long> solvedIds = userProblemRepository.findSolvedProblemIdsByUserId(userId);
        // 해당 챕터 문제 중 풀이 완료한 수만 카운트 (다른 챕터 풀이 이력 제외)
        Set<Long> chapterProblemIdSet = new HashSet<>(allProblemIds);
        int solvedCount = (int) solvedIds.stream().filter(chapterProblemIdSet::contains).count();
        allProblemIds.removeAll(solvedIds);

        // 케이스 2: 모든 문제를 풀었을 때
        if (allProblemIds.isEmpty()) {
            throw new NoMoreProblemsException();
        }

        // Redis에서 현재 노출 중인 문제(1단계 전)와 직전 건너뛴 문제(2단계 전) 조회
        Long prevCurrentId = problemRedisService.getCurrentProblemId(userId, chapterId);
        Long lastSkippedId = problemRedisService.getLastSkippedProblemId(userId, chapterId);

        // 두 문제 모두 제외 시도
        List<Long> candidates = new ArrayList<>(allProblemIds);
        if (prevCurrentId != null) candidates.remove(prevCurrentId);
        if (lastSkippedId != null) candidates.remove(lastSkippedId);

        // 후보가 없으면 우선순위 낮은 lastSkippedId 제외 해제 후 재시도
        if (candidates.isEmpty() && lastSkippedId != null) {
            candidates = new ArrayList<>(allProblemIds);
            if (prevCurrentId != null) candidates.remove(prevCurrentId);
        }

        // 케이스 3: 미풀이 문제는 있으나 현재 노출 중인 문제뿐이라 넘길 수 없을 때
        if (candidates.isEmpty()) {
            throw new NoAvailableProblemsException();
        }

        // 랜덤 1개 선택
        int randomIndex = (int) (Math.random() * candidates.size());
        Long selectedProblemId = candidates.get(randomIndex);

        // Redis 업데이트: 이전 현재 문제 → lastSkipped, 새로 선택된 문제 → current
        if (prevCurrentId != null) {
            problemRedisService.saveLastSkippedProblem(userId, chapterId, prevCurrentId);
        }
        problemRedisService.saveCurrentProblem(userId, chapterId, selectedProblemId);

        Problem problem = problemRepository.findById(selectedProblemId)
                .orElseThrow(() -> new ResourceNotFoundException("문제를 찾을 수 없습니다. id=" + selectedProblemId));

        List<Choice> choices = choiceRepository.findByProblemId(selectedProblemId);
        Integer correctRate = problemRedisService.getCorrectRate(selectedProblemId);

        return new RandomProblemResponse(problem, choices, correctRate, totalCount, solvedCount);
    }
}
