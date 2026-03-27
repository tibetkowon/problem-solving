package com.problemsolving.batch.scheduler;

import com.problemsolving.core.domain.problem.entity.ProblemCorrectRate;
import com.problemsolving.core.repository.ProblemCorrectRateRepository;
import com.problemsolving.core.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 5분 주기로 Redis 정답률 데이터를 DB에 동기화하는 스케줄러
 *
 * Redis 구조: correct_rate:{problemId} → Hash { submit: N, correct: M }
 * DB 구조: problem_correct_rate (problem_id PK, submit_count, correct_count)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CorrectRateSyncScheduler {

    private static final String CORRECT_RATE_KEY_PREFIX = "correct_rate:";
    private static final String FIELD_SUBMIT = "submit";
    private static final String FIELD_CORRECT = "correct";

    private final RedisTemplate<String, String> redisTemplate;
    private final ProblemRepository problemRepository;
    private final ProblemCorrectRateRepository problemCorrectRateRepository;

    @Scheduled(fixedDelay = 5 * 60 * 1000) // 5분
    @Transactional
    public void syncCorrectRateToDb() {
        log.info("[CorrectRateSync] 정답률 DB 동기화 시작");

        List<Long> problemIds = problemRepository.findAll().stream()
                .map(p -> p.getId())
                .toList();

        int syncCount = 0;
        for (Long problemId : problemIds) {
            String key = CORRECT_RATE_KEY_PREFIX + problemId;
            Object submitObj = redisTemplate.opsForHash().get(key, FIELD_SUBMIT);

            if (submitObj == null) continue; // Redis에 데이터 없으면 스킵

            long submitCount = Long.parseLong(submitObj.toString());
            Object correctObj = redisTemplate.opsForHash().get(key, FIELD_CORRECT);
            long correctCount = correctObj != null ? Long.parseLong(correctObj.toString()) : 0;

            problemCorrectRateRepository.findById(problemId).ifPresentOrElse(
                    existing -> existing.update(submitCount, correctCount),
                    () -> problemCorrectRateRepository.save(
                            ProblemCorrectRate.builder()
                                    .problemId(problemId)
                                    .submitCount(submitCount)
                                    .correctCount(correctCount)
                                    .build()
                    )
            );
            syncCount++;
        }

        log.info("[CorrectRateSync] 정답률 DB 동기화 완료 - {}개 문제 업데이트", syncCount);
    }
}
