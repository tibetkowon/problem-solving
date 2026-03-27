package com.problemsolving.core.repository;

import com.problemsolving.core.domain.problem.entity.Choice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChoiceRepository extends JpaRepository<Choice, Long> {

    List<Choice> findByProblemId(Long problemId);

    List<Choice> findByProblemIdAndIsCorrectTrue(Long problemId);
}
