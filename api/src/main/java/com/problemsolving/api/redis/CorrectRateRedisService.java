package com.problemsolving.api.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 정답률 Redis 캐시 서비스
 *
 * Redis Hash 구조:
 *   Key:   correct_rate:{problemId}
 *   Field: submit  → 제출 수
 *          correct → 정답 수
 *
 * 건너뛰기 Redis 구조:
 *   Key:   skip:{userId}:{chapterId}
 *   Value: 직전에 건너뛴 problemId (String)
 *   TTL:   1시간
 */
@Service
@RequiredArgsConstructor
public class CorrectRateRedisService {

    private static final String CORRECT_RATE_KEY_PREFIX = "correct_rate:";
    private static final String SKIP_KEY_PREFIX = "skip:";
    private static final String FIELD_SUBMIT = "submit";
    private static final String FIELD_CORRECT = "correct";
    private static final int MIN_SUBMIT_COUNT = 30;
    private static final Duration SKIP_TTL = Duration.ofHours(1);

    private final RedisTemplate<String, String> redisTemplate;

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
     * 문제 조회 시 정답률을 계산해 반환한다.
     * 제출 수가 30명 미만이면 null을 반환한다.
     * 30명 이상이면 소수점 첫째 자리에서 반올림한 정수(%)를 반환한다.
     */
    public Integer getCorrectRate(Long problemId) {
        String key = correctRateKey(problemId);
        Object submitObj = redisTemplate.opsForHash().get(key, FIELD_SUBMIT);
        Object correctObj = redisTemplate.opsForHash().get(key, FIELD_CORRECT);

        if (submitObj == null) {
            return null;
        }

        long submitCount = Long.parseLong(submitObj.toString());
        if (submitCount < MIN_SUBMIT_COUNT) {
            return null;
        }

        long correctCount = correctObj != null ? Long.parseLong(correctObj.toString()) : 0;
        double rate = (double) correctCount / submitCount * 100;
        return (int) Math.round(rate);
    }

    /**
     * Redis에서 정답률 원본 데이터(제출수, 정답수)를 읽는다.
     * 스케줄러의 DB 백업 시 사용한다.
     */
    public long[] getRawCounts(Long problemId) {
        String key = correctRateKey(problemId);
        Object submitObj = redisTemplate.opsForHash().get(key, FIELD_SUBMIT);
        Object correctObj = redisTemplate.opsForHash().get(key, FIELD_CORRECT);

        long submit = submitObj != null ? Long.parseLong(submitObj.toString()) : 0;
        long correct = correctObj != null ? Long.parseLong(correctObj.toString()) : 0;
        return new long[]{submit, correct};
    }

    // ── 건너뛰기 관련 ────────────────────────────────────────────────

    /**
     * 사용자가 문제를 건너뛸 때 Redis에 직전 건너뛴 문제 ID를 저장한다.
     * TTL 1시간으로 세션성 데이터로 관리한다.
     */
    public void saveSkippedProblem(Long userId, Long chapterId, Long problemId) {
        String key = skipKey(userId, chapterId);
        redisTemplate.opsForValue().set(key, String.valueOf(problemId), SKIP_TTL);
    }

    /**
     * 직전에 건너뛴 문제 ID를 조회한다. 없으면 null을 반환한다.
     */
    public Long getLastSkippedProblemId(Long userId, Long chapterId) {
        String key = skipKey(userId, chapterId);
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : null;
    }

    // ── 키 생성 헬퍼 ────────────────────────────────────────────────

    private String correctRateKey(Long problemId) {
        return CORRECT_RATE_KEY_PREFIX + problemId;
    }

    private String skipKey(Long userId, Long chapterId) {
        return SKIP_KEY_PREFIX + userId + ":" + chapterId;
    }
}
