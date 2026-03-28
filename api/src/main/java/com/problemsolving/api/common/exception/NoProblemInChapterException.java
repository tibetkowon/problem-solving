package com.problemsolving.api.common.exception;

public class NoProblemInChapterException extends RuntimeException {

    public NoProblemInChapterException() {
        super("해당 단원에 등록된 문제가 없습니다.");
    }
}
