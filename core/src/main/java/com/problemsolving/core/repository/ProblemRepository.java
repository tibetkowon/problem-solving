package com.problemsolving.core.repository;

import com.problemsolving.core.domain.problem.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    // 특정 챕터의 전체 문제 ID 조회
    @Query("SELECT p.id FROM Problem p WHERE p.chapter.id = :chapterId")
    List<Long> findIdsByChapterId(@Param("chapterId") Long chapterId);
}
