package com.problemsolving.core.domain.problem.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "choice", indexes = {
        @Index(name = "idx_choice_problem_id", columnList = "problem_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Choice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean isCorrect;

    @Builder
    public Choice(Problem problem, String content, boolean isCorrect) {
        this.problem = problem;
        this.content = content;
        this.isCorrect = isCorrect;
    }
}
