package com.gdamiens.website.controller;

import com.gdamiens.website.exceptions.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Errors of every controller as RFC 9457 problem details, with a stable {@code code} property when the apps act on
 * it (quota, journey budget...)
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ProblemDetail> handleCustomException(CustomException e) {
        return ResponseEntity.status(e.getHttpStatus()).body(e.toProblemDetail());
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ProblemDetail> handleBadRequest(Exception e) {
        return problem(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    /** Malformed JSON body or unknown enum value */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadableBody(HttpMessageNotReadableException e) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request body");
    }

    /** {@code @PreAuthorize} refusal (an admin endpoint called by a user) */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException e) {
        return problem(HttpStatus.FORBIDDEN, "Access denied");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(RuntimeException e) {
        LOGGER.error("Unexpected error", e);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
    }

    private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String detail) {
        return ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(status, detail));
    }
}
