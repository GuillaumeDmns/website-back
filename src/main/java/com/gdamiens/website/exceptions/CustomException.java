package com.gdamiens.website.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public class CustomException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String message;
    private final HttpStatus httpStatus;

    /** Stable error code for the apps (problem detail property {@code code}), null when the status is enough */
    private final String code;

    public CustomException(String message, HttpStatus httpStatus) {
        this(message, httpStatus, null);
    }

    public CustomException(String message, HttpStatus httpStatus, String code) {
        this.message = message;
        this.httpStatus = httpStatus;
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

    public ProblemDetail toProblemDetail() {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(httpStatus, message);
        if (code != null) {
            problem.setProperty("code", code);
        }
        return problem;
    }
}
