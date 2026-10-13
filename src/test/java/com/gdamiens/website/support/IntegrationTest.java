package com.gdamiens.website.support;

import com.gdamiens.website.client.ApiQuota;
import com.gdamiens.website.client.PrimCircuitBreaker;
import com.gdamiens.website.model.GtfsImport;
import com.gdamiens.website.repository.GtfsImportRepository;
import com.gdamiens.website.service.GtfsImportService;
import com.gdamiens.website.support.FakeServer.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base of the integration tests: the whole application on a random port, against a throwaway PostGIS (the image of
 * the CI, migrated by Liquibase) holding the {@link MiniGtfs} network, and a {@link FakeServer} standing for PRIM,
 * Navitia, Opendatasoft (GTFS, elevators, stop towns), the Géoplateforme, Vélib and Google. Every test class shares one application and one database: tests use their
 * own users, client addresses ({@link Api}) and stop areas where state is kept (rate limits, caches).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class IntegrationTest {

    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
        DockerImageName.parse("postgis/postgis:18-3.6").asCompatibleSubstituteFor("postgres"));

    protected static final FakeServer EXTERNAL = FakeServer.start();

    protected static final GoogleTokens GOOGLE = new GoogleTokens();

    /** Version of the GTFS dataset served by default */
    protected static final Instant GTFS_VERSION = Instant.parse("2026-10-10T06:00:00Z");

    private static boolean gtfsImported;

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("application.urls.prim", () -> EXTERNAL.url() + "/prim");
        registry.add("application.urls.gtfs-dataset", () -> EXTERNAL.url() + "/gtfs");
        registry.add("application.urls.velib-gbfs", () -> EXTERNAL.url() + "/velib");
        registry.add("application.urls.elevators", () -> EXTERNAL.url() + "/elevators");
        registry.add("application.urls.stop-zones", () -> EXTERNAL.url() + "/stop-zones");
        registry.add("application.urls.geoplateforme", () -> EXTERNAL.url() + "/geoplateforme");
        registry.add("application.google.jwk-set-uri", () -> EXTERNAL.url() + "/google/certs");
        registry.add("application.monitored-sites[0]", () -> EXTERNAL.url() + "/site/");
        registry.add("application.monitored-sites[1]", () -> EXTERNAL.url() + "/site/down");
    }

    @LocalServerPort
    private int port;

    @Autowired
    protected GtfsImportService gtfsImportService;

    @Autowired
    protected GtfsImportRepository gtfsImportRepository;

    @Autowired
    protected PrimCircuitBreaker circuitBreaker;

    /** A client with no token, at its own address */
    protected Api api;

    @BeforeEach
    void setUpExternalApis() {
        EXTERNAL.reset();
        ExternalApis.stubDefaults(EXTERNAL, GOOGLE);
        serveGtfs(MiniGtfs.network(), GTFS_VERSION);
        api = new Api(port);
        importGtfsOnce();
    }

    /** A test that broke an API leaves it working for the next ones */
    @AfterEach
    void closeCircuits() {
        for (ApiQuota.Api primApi : ApiQuota.Api.values()) {
            circuitBreaker.success(primApi);
        }
    }

    /** The dataset metadata, its records and the zip */
    protected static void serveGtfs(byte[] zip, Instant version) {
        EXTERNAL.on("/gtfs", Response.json("{\"metas\":{\"default\":{\"data_processed\":\"" + version + "\"}}}"));
        EXTERNAL.on("/gtfs/records", Response.json("""
            {"total_count": 2, "results": [
              {"filename": "IDFM_gtfs_light", "url": {"url": "%1$s/gtfs/files/light.zip"}},
              {"filename": "IDFM_gtfs", "url": {"url": "%1$s/gtfs/files/IDFM_gtfs.zip"}}
            ]}""".formatted(EXTERNAL.url())));
        EXTERNAL.on("/gtfs/files/IDFM_gtfs.zip", Response.bytes(zip));
    }

    private synchronized void importGtfsOnce() {
        if (gtfsImported) {
            return;
        }
        gtfsImportService.importGtfs(GtfsImport.MANUAL, GTFS_VERSION);
        assertThat(gtfsImportRepository.findLast()).get().extracting(GtfsImport::status).isEqualTo(GtfsImport.SUCCESS);
        gtfsImported = true;
    }

    /** Signs in a new Google account (admin when [email] is in application.admin-emails) */
    protected SignedIn signInWithGoogle(String email, String firstName) {
        String sub = UUID.randomUUID().toString();
        Api.Response response = api.post("/api/auth/google", Map.of("idToken", GOOGLE.idToken(sub, email, firstName)));
        assertThat(response.status()).as(response.body()).isEqualTo(200);
        return new SignedIn(sub, response.json().path("jwt").asString(), response.json().path("refreshToken").asString(),
            api.as(response.json().path("jwt").asString()));
    }

    /** A new account with a unique email */
    protected SignedIn newUser() {
        return signInWithGoogle("user-" + UUID.randomUUID() + "@example.com", "Ada");
    }

    protected SignedIn admin() {
        return signInWithGoogle("admin@example.com", "Admin");
    }

    /** A guest device's client */
    protected Api guest() {
        Api.Response response = api.post("/api/auth/guest");
        assertThat(response.status()).as(response.body()).isEqualTo(200);
        return api.as(response.json().path("jwt").asString());
    }

    /**
     * @param api client with the access token
     */
    protected record SignedIn(String googleSub, String token, String refreshToken, Api api) {
    }
}
