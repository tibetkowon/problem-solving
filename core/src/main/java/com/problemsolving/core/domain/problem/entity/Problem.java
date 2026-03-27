package com.problemsolving.core.domain.problem.entity;

import com.problemsolving.core.constant.ProblemType;
import com.problemsolving.core.domain.chapter.entity.Chapter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "problem", indexes = {
        @Index(name = "idx_problem_chapter_id", columnList = "chapter_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProblemType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String explanation;

    // 주관식 문제 정답 (객관식은 Choice 에서 관리)
    @Column
    private String answer;

    @Builder
    public Problem(Chapter chapter, String content, ProblemType type, String explanation, String answer) {
        this.chapter = chapter;
        this.content = content;
        this.type = type;
        this.explanation = explanation;
        this.answer = answer;
    }
}
