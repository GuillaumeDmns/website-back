package com.gdamiens.website.client;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.exceptions.NavitiaException;
import com.gdamiens.website.exceptions.PrimUnavailableException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
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
     * Makes a request to [api]: refused at once while {@link PrimCircuitBreaker} has stopped calling it, else counted
     * against its quota ({@link ApiQuota}) and its outcome reported to the circuit breaker. No answer, a 5xx or a 429
     * become a {@link PrimUnavailableException} (a {@link NavitiaException} is rethrown as is, its callers read the
     * status).
     */
    protected <T> T call(ApiQuota.Api api, Supplier<T> request) {
        circuitBreaker.beforeCall(api);
        try {
            apiQuota.consume(api);
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
