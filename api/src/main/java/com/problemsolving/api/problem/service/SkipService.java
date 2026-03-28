package com.problemsolving.api.problem.service;

import com.problemsolving.api.common.exception.ResourceNotFoundException;
import com.problemsolving.api.redis.CorrectRateRedisService;
import com.problemsolving.core.domain.problem.entity.Problem;
import com.problemsolving.core.domain.userproblem.entity.UserProblem;
import com.problemsolving.core.repository.ProblemRepository;
import com.problemsolving.core.repository.UserProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SkipService {

    private final ProblemRepository problemRepository;
    private final UserProblemRepository userProblemRepository;
    private final CorrectRateRedisService correctRateRedisService;

    @Transactional
    public void skip(Long userId, Long problemId, Long chapterId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("문제를 찾을 수 없습니다. id=" + problemId));

        UserProblem skipped = UserProblem.builder()
                .userId(userId)
                .problem(problem)
                .skippedAt(LocalDateTime.now())
                .build();
        userProblemRepository.save(skipped);

        correctRateRedisService.saveSkippedProblem(userId, chapterId, problemId);
    }
}
