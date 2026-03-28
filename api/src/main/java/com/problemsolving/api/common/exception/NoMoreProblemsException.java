package com.problemsolving.api.common.exception;

public class NoMoreProblemsException extends RuntimeException {

    public NoMoreProblemsException() {
        super("해당 단원의 모든 문제를 풀었습니다.");
    }
}
