package com.problemsolving.core.repository;

import com.problemsolving.core.domain.problem.entity.ProblemCorrectRate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemCorrectRateRepository extends JpaRepository<ProblemCorrectRate, Long> {
}
