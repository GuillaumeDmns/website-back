package com.gdamiens.website.exceptions;

import org.springframework.http.HttpStatusCode;

public class NavitiaException extends RuntimeException {

    private final HttpStatusCode statusCode;

    private final String responseBody;

    public NavitiaException(HttpStatusCode statusCode, String responseBody) {
        super("IDFM Navitia answered " + statusCode.value() + ": " + responseBody);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
