package com.gdamiens.website.client;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.exceptions.NavitiaException;
import com.gdamiens.website.exceptions.PrimUnavailableException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.function.Supplier;

/**
 * Base of the PRIM API clients: every request goes through {@link #call}, which applies the daily quotas
 * ({@link ApiQuota}) and the circuit breaker ({@link PrimCircuitBreaker}).
 */
public abstract class PrimClient {

    private final ApplicationProperties applicationProperties;

    private final ApiQuota apiQuota;

    private final PrimCircuitBreaker circuitBreaker;

    protected PrimClient(ApplicationProperties applicationProperties, ApiQuota apiQuota, PrimCircuitBreaker circuitBreaker) {
        this.applicationProperties = applicationProperties;
        this.apiQuota = apiQuota;
        this.circuitBreaker = circuitBreaker;
    }

    /** PRIM marketplace root, without trailing slash */
    protected String primUrl() {
        return applicationProperties.getUrls().getPrim();
    }

    /**
     * Makes a request for [feature]: refused at once while {@link PrimCircuitBreaker} has stopped calling its API,
     * else counted against its quota ({@link ApiQuota}, which also reads PRIM's own count in the answer's headers)
     * and its outcome reported to the circuit breaker. No answer, a 5xx or a 429 become a
     * {@link PrimUnavailableException} (a {@link NavitiaException} is rethrown as is, its callers read the status).
     *
     * @return the body of the answer
     */
    protected <T> T call(ApiQuota.Feature feature, Supplier<ResponseEntity<T>> request) {
        ApiQuota.Api api = feature.api();
        circuitBreaker.beforeCall(api);
        try {
            apiQuota.consume(feature);
        } catch (RuntimeException e) {
            circuitBreaker.abandon(api);
            throw e;
        }
        try {
            ResponseEntity<T> response = request.get();
            circuitBreaker.success(api);
            observeQuota(api, response.getHeaders());
            return response.getBody();
        } catch (ResourceAccessException e) {
            circuitBreaker.failure(api, e.getMessage(), false);
            throw new PrimUnavailableException(api + " did not answer: " + e.getMessage());
        } catch (HttpStatusCodeException e) {
            if (!isOutage(e.getStatusCode())) {
                circuitBreaker.success(api);
                throw e;
            }
            circuitBreaker.failure(api, e.getStatusCode() + " " + e.getStatusText(), isQuotaRefusal(e.getStatusCode()));
            throw new PrimUnavailableException(api + " answered " + e.getStatusCode());
        } catch (NavitiaException e) {
            if (isOutage(e.getStatusCode())) {
                circuitBreaker.failure(api, e.getMessage(), isQuotaRefusal(e.getStatusCode()));
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

    /** {@code x-ratelimit-limit-day} and {@code x-ratelimit-remaining-day}, when PRIM gives them */
    private void observeQuota(ApiQuota.Api api, HttpHeaders headers) {
        Integer limit = intHeader(headers, "x-ratelimit-limit-day");
        Integer remaining = intHeader(headers, "x-ratelimit-remaining-day");
        if (limit != null && remaining != null) {
            apiQuota.observe(api, limit, remaining);
        }
    }

    private static Integer intHeader(HttpHeaders headers, String name) {
        try {
            String value = headers.getFirst(name);
            return value == null ? null : Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean isOutage(HttpStatusCode status) {
        return status.is5xxServerError() || isQuotaRefusal(status);
    }

    private static boolean isQuotaRefusal(HttpStatusCode status) {
        return status.value() == HttpStatus.TOO_MANY_REQUESTS.value();
    }

    /** JSON request with the PRIM API key */
    protected HttpEntity<String> request() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate");
        headers.set("apiKey", applicationProperties.getIdfmKey());
        return new HttpEntity<>(headers);
    }
}
