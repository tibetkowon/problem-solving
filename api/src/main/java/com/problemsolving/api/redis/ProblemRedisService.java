package com.problemsolving.api.redis;

import com.problemsolving.core.repository.ProblemCorrectRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 문제 관련 Redis 서비스
 *
 * [정답률 캐시] Redis Hash 구조:
 *   Key:   correct_rate:{problemId}
 *   Field: submit  → 제출 수
 *          correct → 정답 수
 *   fallback: Redis 미존재 시 DB(problem_correct_rate) 조회 후 캐시 워밍업
 *
 * [현재 노출 문제] Redis String 구조:
 *   Key:   current:{userId}:{chapterId}
 *   Value: 현재 사용자에게 보여주고 있는 problemId
 *   TTL:   1시간
 *
 * [직전 건너뛴 문제] Redis String 구조:
 *   Key:   last_skip:{userId}:{chapterId}
 *   Value: 이전에 보여줬던 problemId (2단계 전 문제)
 *   TTL:   1시간
 */
@Service
@RequiredArgsConstructor
public class ProblemRedisService {

    private static final String CORRECT_RATE_KEY_PREFIX = "correct_rate:";
    private static final String CURRENT_KEY_PREFIX = "current:";
    private static final String LAST_SKIP_KEY_PREFIX = "last_skip:";
    private static final String FIELD_SUBMIT = "submit";
    private static final String FIELD_CORRECT = "correct";
    private static final int MIN_SUBMIT_COUNT = 30;
    private static final Duration SESSION_TTL = Duration.ofHours(1);

    private final RedisTemplate<String, String> redisTemplate;
    private final ProblemCorrectRateRepository problemCorrectRateRepository;

    // ── 정답률 관련 ────────────────────────────────────────────────

    /**
     * 문제 제출 시 Redis 카운터를 원자적으로 증가시킨다.
     * isCorrect가 true면 정답 수도 함께 증가시킨다.
     */
    public void incrementCorrectRate(Long problemId, boolean isCorrect) {
        String key = correctRateKey(problemId);
        redisTemplate.opsForHash().increment(key, FIELD_SUBMIT, 1);
        if (isCorrect) {
            redisTemplate.opsForHash().increment(key, FIELD_CORRECT, 1);
        }
    }

    /**
     * 정답률을 조회한다.
     * Redis에 데이터가 있으면 Redis에서, 없으면 DB에서 조회한다.
     * DB에서 조회한 경우 Redis에 캐싱하여 이후 요청은 Redis에서 처리한다.
     * 제출 수 30명 미만(Redis/DB 모두)이면 null을 반환한다.
     */
    public Integer getCorrectRate(Long problemId) {
        String key = correctRateKey(problemId);
        Object submitObj = redisTemplate.opsForHash().get(key, FIELD_SUBMIT);

        if (submitObj != null) {
            // Redis에 데이터 있음
            long submitCount = Long.parseLong(submitObj.toString());
            if (submitCount < MIN_SUBMIT_COUNT) return null;
            Object correctObj = redisTemplate.opsForHash().get(key, FIELD_CORRECT);
            long correctCount = correctObj != null ? Long.parseLong(correctObj.toString()) : 0;
            return calcRate(correctCount, submitCount);
        }

        // Redis에 데이터 없음 → DB fallback
        return problemCorrectRateRepository.findById(problemId)
                .filter(pcr -> pcr.getSubmitCount() >= MIN_SUBMIT_COUNT)
                .map(pcr -> {
                    // DB 데이터를 Redis에 캐싱 (워밍업)
                    redisTemplate.opsForHash().put(key, FIELD_SUBMIT, String.valueOf(pcr.getSubmitCount()));
                    redisTemplate.opsForHash().put(key, FIELD_CORRECT, String.valueOf(pcr.getCorrectCount()));
                    return calcRate(pcr.getCorrectCount(), pcr.getSubmitCount());
                })
                .orElse(null);
    }

    // ── 현재 노출 문제 관련 ────────────────────────────────────────────────

    /**
     * 현재 사용자에게 보여주고 있는 문제 ID를 Redis에 저장한다.
     */
    public void saveCurrentProblem(Long userId, Long chapterId, Long problemId) {
        redisTemplate.opsForValue().set(currentKey(userId, chapterId), String.valueOf(problemId), SESSION_TTL);
    }

    /**
     * 현재 사용자에게 보여주고 있는 문제 ID를 조회한다. 없으면 null을 반환한다.
     */
    public Long getCurrentProblemId(Long userId, Long chapterId) {
        String value = redisTemplate.opsForValue().get(currentKey(userId, chapterId));
        return value != null ? Long.parseLong(value) : null;
    }

    // ── 직전 건너뛴 문제 관련 ────────────────────────────────────────────────

    /**
     * 직전에 건너뛴 문제 ID를 Redis에 저장한다.
     */
    public void saveLastSkippedProblem(Long userId, Long chapterId, Long problemId) {
        redisTemplate.opsForValue().set(lastSkipKey(userId, chapterId), String.valueOf(problemId), SESSION_TTL);
    }

    /**
     * 직전에 건너뛴 문제 ID를 조회한다. 없으면 null을 반환한다.
     */
    public Long getLastSkippedProblemId(Long userId, Long chapterId) {
        String value = redisTemplate.opsForValue().get(lastSkipKey(userId, chapterId));
        return value != null ? Long.parseLong(value) : null;
    }

    // ── 내부 헬퍼 ────────────────────────────────────────────────

    private int calcRate(long correctCount, long submitCount) {
        return (int) Math.round((double) correctCount / submitCount * 100);
    }

    private String correctRateKey(Long problemId) {
        return CORRECT_RATE_KEY_PREFIX + problemId;
    }

    private String currentKey(Long userId, Long chapterId) {
        return CURRENT_KEY_PREFIX + userId + ":" + chapterId;
    }

    private String lastSkipKey(Long userId, Long chapterId) {
        return LAST_SKIP_KEY_PREFIX + userId + ":" + chapterId;
    }
}
