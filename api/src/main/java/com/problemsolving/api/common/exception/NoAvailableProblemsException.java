package com.problemsolving.api.common.exception;

public class NoAvailableProblemsException extends RuntimeException {

    public NoAvailableProblemsException() {
        super("더 이상 넘길 수 있는 문제가 없습니다. 현재 문제를 풀어주세요.");
    }
}
