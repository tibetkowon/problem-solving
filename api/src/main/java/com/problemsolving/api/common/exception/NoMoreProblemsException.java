package com.problemsolving.api.common.exception;

public class NoMoreProblemsException extends RuntimeException {

    public NoMoreProblemsException() {
        super("해당 단원에 더 이상 제공할 문제가 없습니다.");
    }
}
