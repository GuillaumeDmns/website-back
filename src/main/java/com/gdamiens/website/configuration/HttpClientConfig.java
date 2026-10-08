package com.gdamiens.website.configuration;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.time.Duration;

/**
 * One connection pool for every external API (PRIM, Opendatasoft, Vélib). Apache HttpClient's defaults are 5
 * connections per host and no read timeout: a slow PRIM would queue the requests and hold threads indefinitely.
 */
@Configuration
public class HttpClientConfig {

    /** Most calls answer within a few seconds (Navitia journeys being the slowest) */
    public static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(10);

    /** Big downloads (GTFS zip, network-wide real time): time allowed without receiving data */
    public static final Duration BULK_READ_TIMEOUT = Duration.ofSeconds(60);

    private static final Duration POOL_WAIT = Duration.ofSeconds(2);

    @Bean(destroyMethod = "close")
    public CloseableHttpClient httpClient() {
        return HttpClients.custom()
            .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(100)
                .setMaxConnPerRoute(50)
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                    .setConnectTimeout(Timeout.ofSeconds(3))
                    .setSocketTimeout(Timeout.ofSeconds(60))
                    .setValidateAfterInactivity(TimeValue.ofSeconds(2))
                    .setTimeToLive(TimeValue.ofMinutes(5))
                    .build())
                .build())
            .evictExpiredConnections()
            .evictIdleConnections(TimeValue.ofMinutes(1))
            // Callers fall back (schedule, stale snapshot): no automatic retry, which would also wait for a 429's Retry-After
            .disableAutomaticRetries()
            .build();
    }

    /**
     * Request factory on the shared client. Not a bean: it would close the shared client when destroyed.
     *
     * @param readTimeout time allowed without receiving data
     */
    public static HttpComponentsClientHttpRequestFactory requestFactory(HttpClient httpClient, Duration readTimeout) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectionRequestTimeout(POOL_WAIT);
        factory.setReadTimeout(readTimeout);
        return factory;
    }
}
