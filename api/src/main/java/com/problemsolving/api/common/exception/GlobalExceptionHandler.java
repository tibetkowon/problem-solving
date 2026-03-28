package com.problemsolving.api.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 단원에 문제가 0개 → 404 */
    @ExceptionHandler(NoProblemInChapterException.class)
    public ResponseEntity<Map<String, String>> handleNoProblemInChapter(NoProblemInChapterException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", e.getMessage()));
    }

    /** 모든 문제를 풀었을 때 → 204 */
    @ExceptionHandler(NoMoreProblemsException.class)
    public ResponseEntity<Void> handleNoMoreProblems(NoMoreProblemsException e) {
        return ResponseEntity.noContent().build();
    }

    /** 미풀이 문제는 있으나 현재 노출 중인 문제뿐이라 넘길 수 없을 때 → 409 */
    @ExceptionHandler(NoAvailableProblemsException.class)
    public ResponseEntity<Map<String, String>> handleNoAvailableProblems(NoAvailableProblemsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}
