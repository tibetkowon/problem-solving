package com.problemsolving.core.domain.userproblem.entity;

import com.problemsolving.core.constant.AnswerStatus;
import com.problemsolving.core.domain.problem.entity.Problem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_problem", indexes = {
        @Index(name = "idx_user_problem_user_id_problem_id", columnList = "user_id, problem_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    // 주관식 제출 답변
    @Column
    private String subjectiveAnswer;

    @Enumerated(EnumType.STRING)
    @Column
    private AnswerStatus answerStatus;

    // 문제를 건너뛴 시각 (null이면 제출한 문제)
    @Column
    private LocalDateTime skippedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public UserProblem(Long userId, Problem problem, String subjectiveAnswer,
                       AnswerStatus answerStatus, LocalDateTime skippedAt) {
        this.userId = userId;
        this.problem = problem;
        this.subjectiveAnswer = subjectiveAnswer;
        this.answerStatus = answerStatus;
        this.skippedAt = skippedAt;
    }

    public boolean isSkipped() {
        return this.skippedAt != null;
    }
}
