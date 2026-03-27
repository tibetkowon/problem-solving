package com.problemsolving.core.domain.problem.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "problem_correct_rate")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProblemCorrectRate {

    @Id
    @Column(name = "problem_id")
    private Long problemId;

    @Column(nullable = false)
    private long submitCount;

    @Column(nullable = false)
    private long correctCount;

    @Builder
    public ProblemCorrectRate(Long problemId, long submitCount, long correctCount) {
        this.problemId = problemId;
        this.submitCount = submitCount;
        this.correctCount = correctCount;
    }

    public void update(long submitCount, long correctCount) {
        this.submitCount = submitCount;
        this.correctCount = correctCount;
    }
}
