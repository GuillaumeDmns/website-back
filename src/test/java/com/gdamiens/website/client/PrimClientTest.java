package com.gdamiens.website.client;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.exceptions.NavitiaException;
import com.gdamiens.website.exceptions.PrimUnavailableException;
import com.gdamiens.website.exceptions.QuotaExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.Clock;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class PrimClientTest {

    private static final ApiQuota.Api API = ApiQuota.Api.NAVITIA;

    private static final ApiQuota.Feature FEATURE = ApiQuota.Feature.JOURNEYS;

    private final ApiQuota quota = mock(ApiQuota.class);

    private final PrimCircuitBreaker breaker = mock(PrimCircuitBreaker.class);

    private TestClient client;

    /** Exposes the protected methods */
    private static final class TestClient extends PrimClient {
        TestClient(ApplicationProperties properties, ApiQuota quota, PrimCircuitBreaker breaker) {
            super(properties, quota, breaker);
        }

        <T> T get(Supplier<T> request) {
            return call(FEATURE, () -> ResponseEntity.ok(request.get()));
        }

        <T> T getWithHeaders(HttpHeaders headers, T body) {
            return call(FEATURE, () -> ResponseEntity.ok().headers(headers).body(body));
        }
    }

    @BeforeEach
    void setUp() {
        ApplicationProperties properties = new ApplicationProperties();
        properties.setIdfmKey("secret-key");
        client = new TestClient(properties, quota, breaker);
    }

    @Test
    void countsAndReportsASuccess() {
        assertThat(client.get(() -> "ok")).isEqualTo("ok");

        verify(breaker).beforeCall(API);
        verify(quota).consume(FEATURE);
        verify(breaker).success(API);
    }

    @Test
    void noAnswerIsAnOutage() {
        assertThatThrownBy(() -> client.get(() -> {
            throw new ResourceAccessException("Read timed out");
        })).isInstanceOf(PrimUnavailableException.class);

        verify(breaker).failure(eq(API), anyString(), eq(false));
    }

    @Test
    void aServerErrorIsAnOutage() {
        assertThatThrownBy(() -> client.get(() -> {
            throw new HttpServerErrorException(HttpStatus.BAD_GATEWAY);
        })).isInstanceOfSatisfying(PrimUnavailableException.class,
            e -> assertThat(e.getCode()).isEqualTo(PrimUnavailableException.PRIM_UNAVAILABLE));

        verify(breaker).failure(eq(API), anyString(), eq(false));
    }

    @Test
    void aQuotaRefusalIsReportedAsSuch() {
        assertThatThrownBy(() -> client.get(() -> {
            throw new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS);
        })).isInstanceOf(PrimUnavailableException.class);

        verify(breaker).failure(eq(API), anyString(), eq(true));
    }

    @Test
    void aClientErrorIsAnAnswer() {
        assertThatThrownBy(() -> client.get(() -> {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        })).isInstanceOf(HttpClientErrorException.class);

        verify(breaker).success(API);
    }

    @Test
    void navitiaErrorsKeepTheirStatus() {
        assertThatThrownBy(() -> client.get(() -> {
            throw new NavitiaException(HttpStatus.NOT_FOUND, "{\"error\":{\"id\":\"no_solution\"}}");
        })).isInstanceOf(NavitiaException.class);
        verify(breaker).success(API);

        assertThatThrownBy(() -> client.get(() -> {
            throw new NavitiaException(HttpStatus.SERVICE_UNAVAILABLE, "down");
        })).isInstanceOf(NavitiaException.class);
        verify(breaker).failure(eq(API), anyString(), eq(false));
    }

    @Test
    void aQuotaRefusalBeforeTheCallAbandonsIt() {
        doThrow(new QuotaExceededException("used up", QuotaExceededException.QUOTA_EXHAUSTED)).when(quota).consume(FEATURE);

        assertThatThrownBy(() -> client.get(() -> "never")).isInstanceOf(QuotaExceededException.class);

        verify(breaker).abandon(API);
        verify(breaker, never()).success(API);
    }

    @Test
    void anOpenCircuitIsNotCounted() {
        PrimCircuitBreaker realBreaker = new PrimCircuitBreaker(Clock.systemUTC());
        for (int i = 0; i < PrimCircuitBreaker.FAILURES; i++) {
            realBreaker.failure(API, "down", false);
        }
        TestClient guarded = new TestClient(new ApplicationProperties(), quota, realBreaker);

        assertThatThrownBy(() -> guarded.get(() -> "never")).isInstanceOf(PrimUnavailableException.class);

        verify(quota, never()).consume(FEATURE);
    }

    @Test
    void readsPrimsCountInTheAnswer() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-ratelimit-limit-day", "20000");
        headers.set("x-ratelimit-remaining-day", "19996");

        assertThat(client.getWithHeaders(headers, "ok")).isEqualTo("ok");

        verify(quota).observe(API, 20000, 19996);
    }

    @Test
    void ignoresAnswersWithoutCount() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-ratelimit-limit-day", "not a number");

        client.getWithHeaders(headers, "ok");

        verify(quota, never()).observe(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyInt(),
            org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void sendsTheApiKey() {
        HttpHeaders headers = client.request().getHeaders();

        assertThat(headers.getFirst("apiKey")).isEqualTo("secret-key");
    }
}
