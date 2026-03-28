package com.problemsolving.core.repository;

import com.problemsolving.core.domain.userproblem.entity.UserProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserProblemRepository extends JpaRepository<UserProblem, Long> {

    // 사용자가 제출(풀이 완료)한 문제 ID 목록 조회
    @Query("SELECT up.problem.id FROM UserProblem up WHERE up.userId = :userId AND up.skippedAt IS NULL")
    List<Long> findSolvedProblemIdsByUserId(@Param("userId") Long userId);

    // 사용자가 푼 문제 목록 (풀이 완료, 최신순)
    @Query("""
            SELECT up FROM UserProblem up
            JOIN FETCH up.problem p
            WHERE up.userId = :userId AND up.skippedAt IS NULL
            ORDER BY up.createdAt DESC
            """)
    List<UserProblem> findSolvedHistoryByUserId(@Param("userId") Long userId);

    // 특정 문제의 사용자 풀이 결과 조회
    @Query("""
            SELECT up FROM UserProblem up
            WHERE up.userId = :userId AND up.problem.id = :problemId AND up.skippedAt IS NULL
            ORDER BY up.createdAt DESC
            LIMIT 1
            """)
    Optional<UserProblem> findLatestSolvedByUserIdAndProblemId(
            @Param("userId") Long userId, @Param("problemId") Long problemId);
}
