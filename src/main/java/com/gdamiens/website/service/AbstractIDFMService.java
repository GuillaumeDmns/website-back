package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.exceptions.NavitiaException;
import com.gdamiens.website.exceptions.PrimUnavailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

import java.util.Collections;
import java.util.function.Supplier;

public abstract class AbstractIDFMService {

    private final ApplicationProperties applicationProperties;

    private final String idfmStaticKey;

    protected AbstractIDFMService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.idfmStaticKey = this.applicationProperties.getIdfmStaticKey();
    }

    private ApiQuota apiQuota;

    @Autowired
    void setApiQuota(ApiQuota apiQuota) {
        this.apiQuota = apiQuota;
    }

    private PrimCircuitBreaker circuitBreaker;

    @Autowired
    void setCircuitBreaker(PrimCircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    /** Counts a request against the daily quota of [api] (refused when used up) */
    protected void consume(ApiQuota.Api api) {
        if (apiQuota != null) {
            apiQuota.consume(api);
        }
    }

    /**
     * Makes a request to [api]: refused at once while {@link PrimCircuitBreaker} has stopped calling it, else counted
     * against its quota ({@link ApiQuota}) and its outcome reported to the circuit breaker. No answer, a 5xx or a 429
     * become a {@link PrimUnavailableException} (a {@link NavitiaException} is rethrown as is, its callers read the
     * status).
     */
    protected <T> T call(ApiQuota.Api api, Supplier<T> request) {
        if (circuitBreaker == null) {
            consume(api);
            return request.get();
        }
        circuitBreaker.beforeCall(api);
        try {
            consume(api);
        } catch (RuntimeException e) {
            circuitBreaker.abandon(api);
            throw e;
        }
        try {
            T result = request.get();
            circuitBreaker.success(api);
            return result;
        } catch (ResourceAccessException e) {
            circuitBreaker.failure(api, e.getMessage(), false);
            throw new PrimUnavailableException(api + " did not answer: " + e.getMessage());
        } catch (HttpStatusCodeException e) {
            if (!isOutage(e.getStatusCode())) {
                circuitBreaker.success(api);
                throw e;
            }
            circuitBreaker.failure(api, e.getStatusCode() + " " + e.getStatusText(), e.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value());
            throw new PrimUnavailableException(api + " answered " + e.getStatusCode());
        } catch (NavitiaException e) {
            if (isOutage(e.getStatusCode())) {
                circuitBreaker.failure(api, e.getMessage(), e.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value());
            } else {
                circuitBreaker.success(api);
            }
            throw e;
        } catch (RuntimeException e) {
            // An answer that can't be read: PRIM is up
            circuitBreaker.success(api);
            throw e;
        }
    }

    private static boolean isOutage(HttpStatusCode status) {
        return status.is5xxServerError() || status.value() == HttpStatus.TOO_MANY_REQUESTS.value();
    }

    public String getIdfmStaticKey() {
        return idfmStaticKey;
    }

    protected HttpEntity<String> prepareHttpRequest() {
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Accept-encoding", "gzip, deflate");
        headers.set("apiKey", this.applicationProperties.getIdfmKey());

        return new HttpEntity<>(headers);
    }
}
