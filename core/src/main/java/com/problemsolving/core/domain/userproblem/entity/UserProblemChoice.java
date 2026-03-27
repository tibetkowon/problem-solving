package com.problemsolving.core.domain.userproblem.entity;

import com.problemsolving.core.domain.problem.entity.Choice;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_problem_choice", indexes = {
        @Index(name = "idx_user_problem_choice_user_problem_id", columnList = "user_problem_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProblemChoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_problem_id", nullable = false)
    private UserProblem userProblem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "choice_id", nullable = false)
    private Choice choice;

    @Builder
    public UserProblemChoice(UserProblem userProblem, Choice choice) {
        this.userProblem = userProblem;
        this.choice = choice;
    }
}
