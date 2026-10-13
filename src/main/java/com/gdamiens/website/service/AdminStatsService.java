package com.gdamiens.website.service;

import com.gdamiens.website.client.ApiQuota;
import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.controller.object.v2.LineSummary;
import com.gdamiens.website.model.GtfsImport;
import com.gdamiens.website.model.TransportMode;
import com.gdamiens.website.repository.AdminStatsRepository;
import com.gdamiens.website.repository.AdminStatsRepository.AccountCounts;
import com.gdamiens.website.repository.AdminStatsRepository.Calendar;
import com.gdamiens.website.repository.AdminStatsRepository.DatabaseInfo;
import com.gdamiens.website.repository.AdminStatsRepository.DayCount;
import com.gdamiens.website.repository.AdminStatsRepository.FavoriteTarget;
import com.gdamiens.website.repository.AdminStatsRepository.GroupCount;
import com.gdamiens.website.repository.AdminStatsRepository.QuotaDay;
import com.gdamiens.website.repository.AdminStatsRepository.TableSize;
import com.gdamiens.website.repository.GtfsImportRepository;
import com.gdamiens.website.repository.NetworkRepository;
import com.gdamiens.website.utils.ParisTime;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLSession;
import java.io.File;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Usage statistics of the administration, from what the service already keeps: accounts and their sessions, favorites,
 * PRIM calls per day, the GTFS, the database and the JVM, and checks of the public pages. Anonymous: counts only.
 */
@Service
public class AdminStatsService {

    /** Days of history at most */
    public static final int MAX_DAYS = 366;

    private static final int TOP = 10;

    /** Sites checked again after this long at most (the page reloads them) */
    private static final Duration SITES_MAX_AGE = Duration.ofMinutes(1);

    private static final Duration SITE_TIMEOUT = Duration.ofSeconds(5);

    private final AdminStatsRepository repository;

    private final NetworkRepository networkRepository;

    private final NetworkService networkService;

    private final GtfsImportRepository gtfsImportRepository;

    private final GtfsImportService gtfsImportService;

    private final ApiQuota apiQuota;

    private final ApplicationProperties applicationProperties;

    private final ObjectProvider<BuildProperties> buildProperties;

    private final Clock clock;

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(SITE_TIMEOUT)
        .followRedirects(HttpClient.Redirect.NEVER)
        .build();

    private volatile Sites sites;

    public AdminStatsService(AdminStatsRepository repository, NetworkRepository networkRepository, NetworkService networkService,
                             GtfsImportRepository gtfsImportRepository, GtfsImportService gtfsImportService, ApiQuota apiQuota,
                             ApplicationProperties applicationProperties, ObjectProvider<BuildProperties> buildProperties, Clock clock) {
        this.repository = repository;
        this.networkRepository = networkRepository;
        this.networkService = networkService;
        this.gtfsImportRepository = gtfsImportRepository;
        this.gtfsImportService = gtfsImportService;
        this.apiQuota = apiQuota;
        this.applicationProperties = applicationProperties;
        this.buildProperties = buildProperties;
        this.clock = clock;
    }

    /**
     * @param signUps        accounts created per day over the period (days without one left out)
     * @param activePerDay   accounts that used the app per day, last 7 days only (sessions renewed every hour of use)
     * @param lastSignIns    accounts per time since their last sign-in: today, 7d, 30d, 90d, older, never
     * @param favoritesPerAccount accounts per number of favorites: 0, 1, 2-4, 5-9, 10+
     */
    public record Accounts(AccountCounts counts, List<DayCount> signUps, List<DayCount> activePerDay, List<GroupCount> lastSignIns,
                           List<GroupCount> favoritesPerAccount) {
    }

    public record TopStop(String id, String name, int count) {
    }

    public record TopLine(LineSummary line, int count) {
    }

    /**
     * @param perKind HOME, WORK, PLACE, STOP, LINE (positions are never given, only counted)
     * @param created favorites added per day over the period
     */
    public record Favorites(int total, int accounts, List<GroupCount> perKind, List<TopStop> stops, List<TopLine> lines,
                            List<DayCount> created) {
    }

    /** A PRIM API and its daily quota: what PRIM allows, and what this server allows itself (90 %, guests' share) */
    public record ApiLimit(ApiQuota.Api api, int perDay, int limit, int guestLimit) {
    }

    /** @param days calls per day and feature, today's from the live counters */
    public record Quotas(List<ApiLimit> apis, List<QuotaDay> days) {
    }

    /**
     * @param publishedVersion version of the GTFS published by IDFM now (null when unknown)
     * @param importedVersion  version of the GTFS in use
     */
    public record Network(Map<TransportMode, Integer> linesPerMode, int stopAreas, int quays, Calendar calendar,
                          Instant publishedVersion, Instant importedVersion, DatabaseInfo database, List<TableSize> tables) {
    }

    /**
     * @param builtAt     when the running jar was built (null without build information, e.g. from the IDE)
     * @param diskFree    free space where the GTFS import builds the new schema (temporary directory)
     * @param processCpu  share of the machine's CPU used by the JVM (0-1, null when unknown)
     */
    public record SystemInfo(String java, String springBoot, Instant builtAt, Instant startedAt, long uptimeSeconds, int processors,
                             Double processCpu, Double systemCpu, Double loadAverage, long heapUsed, long heapMax, long nonHeapUsed,
                             Long memoryTotal, Long memoryFree, int threads, int peakThreads, long gcCount, long gcMillis,
                             long diskFree, long diskTotal) {
    }

    /**
     * @param status            HTTP status (redirects not followed), null without answer
     * @param certificateExpiry end of validity of the TLS certificate, null over HTTP
     */
    public record SiteCheck(String url, Integer status, Long millis, Instant certificateExpiry, String error) {
    }

    public record Sites(Instant checkedAt, List<SiteCheck> sites) {
    }

    public Accounts accounts(int days) {
        LocalDate today = today();
        return new Accounts(repository.accountCounts(today), repository.signUpsPerDay(from(today, days)),
            repository.activeAccountsPerDay(today.minusDays(6)), repository.lastSignIns(today), repository.favoritesPerAccount());
    }

    public Favorites favorites(int days) {
        List<GroupCount> perKind = repository.favoritesPerKind();
        List<FavoriteTarget> topStops = repository.topFavoriteStops(TOP);
        Map<String, NetworkRepository.StopAreaRow> stopAreas = networkRepository.findStopAreas(topStops.stream().map(FavoriteTarget::id).toList());
        List<TopStop> stops = topStops.stream()
            .map(stop -> new TopStop(stop.id(), Optional.ofNullable(stopAreas.get(stop.id())).map(NetworkRepository.StopAreaRow::name).orElse(null),
                stop.count()))
            .toList();
        List<TopLine> lines = repository.topFavoriteLines(TOP).stream()
            .map(line -> new TopLine(networkService.getLine(line.id())
                .orElse(new LineSummary(line.id(), line.id(), null, null, null, null)), line.count()))
            .toList();
        return new Favorites(perKind.stream().mapToInt(GroupCount::count).sum(), repository.accountsWithFavorites(), perKind, stops, lines,
            repository.favoritesPerDay(from(today(), days)));
    }

    public Quotas quotas(int days) {
        Map<ApiQuota.Api, ApiQuota.Usage> usage = new EnumMap<>(ApiQuota.Api.class);
        apiQuota.usage().forEach(api -> usage.put(api.api(), api));
        List<ApiLimit> apis = usage.values().stream()
            .map(api -> new ApiLimit(api.api(), api.api().perDay(), api.limit(), api.guestLimit()))
            .toList();
        LocalDate today = apiQuota.day();
        List<QuotaDay> history = new ArrayList<>(repository.quotaDays(from(today, days)).stream()
            .filter(day -> !day.day().equals(today))
            .toList());
        apiQuota.featureUsage().forEach(feature -> history.add(new QuotaDay(today, feature.feature().name(), feature.api().name(),
            feature.calls(), feature.guestCalls())));
        return new Quotas(apis, history);
    }

    public Network network() {
        Map<TransportMode, Integer> linesPerMode = new EnumMap<>(TransportMode.class);
        networkService.getLines().values().forEach(line -> {
            if (line.mode() != null) {
                linesPerMode.merge(line.mode(), 1, Integer::sum);
            }
        });
        Map<String, Integer> stops = repository.stopCounts();
        Instant imported = gtfsImportRepository.findLastSuccess().map(GtfsImport::feedDate).orElse(null);
        return new Network(linesPerMode, stops.getOrDefault("stopAreas", 0), stops.getOrDefault("quays", 0), repository.calendar(),
            gtfsImportService.publishedVersion().orElse(null), imported, repository.database(), repository.tableSizes());
    }

    public SystemInfo system() {
        var runtime = ManagementFactory.getRuntimeMXBean();
        var memory = ManagementFactory.getMemoryMXBean();
        var threads = ManagementFactory.getThreadMXBean();
        MemoryUsage heap = memory.getHeapMemoryUsage();
        long gcCount = 0;
        long gcMillis = 0;
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            gcCount += Math.max(gc.getCollectionCount(), 0);
            gcMillis += Math.max(gc.getCollectionTime(), 0);
        }
        Double processCpu = null;
        Double systemCpu = null;
        Long memoryTotal = null;
        Long memoryFree = null;
        if (ManagementFactory.getOperatingSystemMXBean() instanceof com.sun.management.OperatingSystemMXBean os) {
            processCpu = ratio(os.getProcessCpuLoad());
            systemCpu = ratio(os.getCpuLoad());
            memoryTotal = os.getTotalMemorySize();
            memoryFree = os.getFreeMemorySize();
        }
        double load = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
        File disk = new File(System.getProperty("java.io.tmpdir"));
        BuildProperties build = buildProperties.getIfAvailable();
        return new SystemInfo(Runtime.version().toString(), SpringBootVersion.getVersion(), build == null ? null : build.getTime(),
            Instant.ofEpochMilli(runtime.getStartTime()), runtime.getUptime() / 1000, Runtime.getRuntime().availableProcessors(),
            processCpu, systemCpu, load < 0 ? null : load, heap.getUsed(), heap.getMax(), memory.getNonHeapMemoryUsage().getUsed(),
            memoryTotal, memoryFree, threads.getThreadCount(), threads.getPeakThreadCount(), gcCount, gcMillis, disk.getUsableSpace(),
            disk.getTotalSpace());
    }

    /** The monitored pages, checked in parallel at most once a minute */
    public Sites sites() {
        Sites current = sites;
        if (current != null && current.checkedAt().plus(SITES_MAX_AGE).isAfter(clock.instant())) {
            return current;
        }
        synchronized (this) {
            if (sites != current) {
                return sites;
            }
            List<String> urls = applicationProperties.getMonitoredSites();
            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<Future<SiteCheck>> checks = urls.stream().map(url -> executor.submit(() -> check(url))).toList();
                List<SiteCheck> results = new ArrayList<>();
                for (int i = 0; i < checks.size(); i++) {
                    try {
                        results.add(checks.get(i).get());
                    } catch (Exception e) {
                        results.add(new SiteCheck(urls.get(i), null, null, null, e.getMessage()));
                    }
                }
                sites = new Sites(clock.instant(), results);
            }
            return sites;
        }
    }

    private SiteCheck check(String url) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(SITE_TIMEOUT).GET().build();
        long start = System.nanoTime();
        try {
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            long millis = Duration.ofNanos(System.nanoTime() - start).toMillis();
            Instant expiry = response.sslSession().map(AdminStatsService::certificateExpiry).orElse(null);
            return new SiteCheck(url, response.statusCode(), millis, expiry, null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new SiteCheck(url, null, null, null, "interrupted");
        } catch (Exception e) {
            return new SiteCheck(url, null, null, null, e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage()));
        }
    }

    private static Instant certificateExpiry(SSLSession session) {
        try {
            return session.getPeerCertificates()[0] instanceof X509Certificate certificate ? certificate.getNotAfter().toInstant() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static Double ratio(double value) {
        return value < 0 || Double.isNaN(value) ? null : value;
    }

    private LocalDate today() {
        return LocalDate.now(clock.withZone(ParisTime.ZONE));
    }

    /** The first day of a period of [days] days ending today */
    private static LocalDate from(LocalDate today, int days) {
        return today.minusDays(Math.clamp(days, 1, MAX_DAYS) - 1L);
    }
}
