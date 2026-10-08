package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.configuration.HttpClientConfig;
import com.gdamiens.website.model.GtfsImport;
import com.gdamiens.website.repository.GtfsImportRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class GtfsImportService {

    private static final Logger log = LoggerFactory.getLogger(GtfsImportService.class);

    private static final String STAGING_SCHEMA = "gtfs_new";

    private static final String SCHEMA = "gtfs";

    private static final Map<String, String> FILE_TO_TABLE = Map.ofEntries(
        Map.entry("agency.txt", "agency"),
        Map.entry("routes.txt", "routes"),
        Map.entry("trips.txt", "trips"),
        Map.entry("calendar.txt", "calendar"),
        Map.entry("calendar_dates.txt", "calendar_dates"),
        Map.entry("stops.txt", "stops"),
        Map.entry("stop_times.txt", "stop_times"),
        Map.entry("transfers.txt", "transfers"),
        Map.entry("pathways.txt", "pathways"),
        Map.entry("shapes.txt", "shape_points"),
        Map.entry("booking_rules.txt", "booking_rules"),
        Map.entry("ticketing_deep_links.txt", "ticketing_deep_links"),
        Map.entry("attributions.txt", "attributions"),
        Map.entry("object_codes_extension.txt", "object_codes_extension"),
        Map.entry("stop_extensions.txt", "object_codes_extension")
    );

    private static final Set<String> MANDATORY_TABLES = Set.of("agency", "routes", "trips", "stops", "stop_times");

    // IDFM headers that differ from the GTFS spec
    private static final Map<String, String> COLUMN_ALIASES = Map.of(
        "pathways_id", "pathway_id",
        "min_windth", "min_width",
        "transversal_time", "traversal_time"
    );

    // A new import must keep at least this ratio of the current rows, otherwise the feed is considered truncated
    private static final double MIN_ROW_RATIO = 0.5;

    private final DataSource dataSource;

    private final IDFMMainService idfmMainService;

    private final TaskExecutor taskExecutor;

    private final RestTemplate restTemplate;

    private final ReentrantLock lock = new ReentrantLock();

    private final GtfsImportRepository gtfsImportRepository;

    private final ApplicationEventPublisher eventPublisher;

    private final long minFreeDiskBytes;

    public GtfsImportService(DataSource dataSource, IDFMMainService idfmMainService, @Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor,
                             HttpClient httpClient, GtfsImportRepository gtfsImportRepository, ApplicationEventPublisher eventPublisher,
                             ApplicationProperties applicationProperties) {
        this.dataSource = dataSource;
        this.idfmMainService = idfmMainService;
        this.taskExecutor = taskExecutor;
        this.restTemplate = new RestTemplate(HttpClientConfig.requestFactory(httpClient, HttpClientConfig.BULK_READ_TIMEOUT));
        this.gtfsImportRepository = gtfsImportRepository;
        this.eventPublisher = eventPublisher;
        this.minFreeDiskBytes = applicationProperties.getGtfs().getMinFreeDiskGb() * 1024L * 1024 * 1024;
    }

    /**
     * Launches an import in background, whatever the version already imported.
     *
     * @return false if an import is already running
     */
    public boolean importGtfsAsync() {
        if (this.lock.isLocked()) {
            return false;
        }

        this.taskExecutor.execute(() -> this.importGtfs(GtfsImport.MANUAL, this.publishedVersion().orElse(null)));
        return true;
    }

    /**
     * @return the version of the GTFS dataset published now (Opendatasoft processing date), empty when unknown
     */
    public Optional<Instant> publishedVersion() {
        try {
            return this.idfmMainService.getGtfsDataDate();
        } catch (RuntimeException e) {
            log.warn("GTFS dataset version unavailable: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Imports the GTFS published now and records it in {@code public.gtfs_import}. On success, {@link GtfsImportedEvent}
     * is published; on failure the current data stay.
     *
     * @param source   {@link GtfsImport#WATCH} or {@link GtfsImport#MANUAL}
     * @param feedDate version expected (recorded with the import), null when unknown
     */
    public void importGtfs(String source, Instant feedDate) {
        if (!this.lock.tryLock()) {
            log.warn("GTFS import already running, skipping");
            return;
        }

        Path zipPath = null;
        Long importId = null;
        try {
            long start = System.currentTimeMillis();
            log.info("Start GTFS import ({}, version {})", source, feedDate);
            importId = this.gtfsImportRepository.start(source, feedDate);
            this.checkFreeDisk();

            String gtfsLink = this.idfmMainService.getGTFSlink();
            if (gtfsLink == null) {
                throw new IllegalStateException("No GTFS link found in the dataset");
            }

            zipPath = this.download(gtfsLink);
            Map<String, Long> rowsByTable = this.load(zipPath);
            this.gtfsImportRepository.finish(importId, GtfsImport.SUCCESS, describe(rowsByTable), null);
            this.eventPublisher.publishEvent(new GtfsImportedEvent());

            log.info("Finish GTFS import (took {}s)", (System.currentTimeMillis() - start) / 1000);
        } catch (Exception e) {
            log.error("GTFS import failed, current data kept", e);
            if (importId != null) {
                this.gtfsImportRepository.finish(importId, GtfsImport.FAILED, null, StringUtils.abbreviate(e.toString(), 2000));
            }
        } finally {
            if (zipPath != null) {
                try {
                    Files.deleteIfExists(zipPath);
                } catch (IOException e) {
                    log.warn("Unable to delete {}", zipPath, e);
                }
            }
            this.lock.unlock();
        }
    }

    /** The new schema is built next to the current one, and the zip waits in the temporary directory (same disk) */
    private void checkFreeDisk() throws IOException {
        long free = Files.getFileStore(Path.of(System.getProperty("java.io.tmpdir"))).getUsableSpace();
        if (free < this.minFreeDiskBytes) {
            throw new IllegalStateException("Not enough free disk for a GTFS import: " + free / (1024 * 1024) + " MB, at least "
                + this.minFreeDiskBytes / (1024 * 1024) + " MB needed");
        }
    }

    private static String describe(Map<String, Long> rowsByTable) {
        return rowsByTable.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining(", "));
    }

    private Path download(String link) throws IOException {
        Path zipPath = Files.createTempFile("gtfs-", ".zip");
        long start = System.currentTimeMillis();

        this.restTemplate.execute(link, HttpMethod.GET, request -> request.getHeaders().set("Authorization", "apikey " + this.idfmMainService.getIdfmStaticKey()), response -> {
            Files.copy(response.getBody(), zipPath, StandardCopyOption.REPLACE_EXISTING);
            return null;
        });

        log.info("GTFS downloaded: {} MB (took {}ms)", Files.size(zipPath) / (1024 * 1024), System.currentTimeMillis() - start);
        return zipPath;
    }

    /** @return rows loaded per table */
    private Map<String, Long> load(Path zipPath) throws SQLException, IOException {
        try (Connection connection = this.dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("SET LOCAL maintenance_work_mem = '512MB'");
                    statement.execute("SET LOCAL synchronous_commit = off");
                }

                List<String> tables = this.getTables(connection);
                this.createStagingTables(connection, tables);

                Map<String, Long> rowsByTable = this.copyFiles(connection, zipPath);
                this.checkRows(connection, rowsByTable);

                long start = System.currentTimeMillis();
                ScriptUtils.executeSqlScript(connection, new ClassPathResource("gtfs/post_load.sql"));
                this.copyKeysAndIndexes(connection);
                this.analyze(connection, tables);
                log.info("GTFS keys, indexes and statistics built (took {}ms)", System.currentTimeMillis() - start);

                try (Statement statement = connection.createStatement()) {
                    statement.execute("DROP SCHEMA IF EXISTS " + SCHEMA + " CASCADE");
                    statement.execute("ALTER SCHEMA " + STAGING_SCHEMA + " RENAME TO " + SCHEMA);
                }

                connection.commit();
                return rowsByTable;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private List<String> getTables(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT tablename FROM pg_tables WHERE schemaname = ? ORDER BY tablename")) {
            statement.setString(1, SCHEMA);
            List<String> tables = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tables.add(resultSet.getString(1));
                }
            }
            if (tables.isEmpty()) {
                throw new IllegalStateException("Schema " + SCHEMA + " has no table, Liquibase migrations have not been applied");
            }
            return tables;
        }
    }

    // Columns, defaults, generated columns and checks only: keys and indexes are built after the load, which is much faster
    private void createStagingTables(Connection connection, List<String> tables) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP SCHEMA IF EXISTS " + STAGING_SCHEMA + " CASCADE");
            statement.execute("CREATE SCHEMA " + STAGING_SCHEMA);
            for (String table : tables) {
                statement.execute("CREATE TABLE " + STAGING_SCHEMA + "." + table + " (LIKE " + SCHEMA + "." + table
                    + " INCLUDING DEFAULTS INCLUDING GENERATED INCLUDING IDENTITY INCLUDING CONSTRAINTS)");
            }
        }
    }

    private void copyKeysAndIndexes(Connection connection) throws SQLException {
        List<String> ddl = new ArrayList<>();

        String constraintsSql = "SELECT t.relname, c.conname, pg_get_constraintdef(c.oid) FROM pg_constraint c "
            + "JOIN pg_class t ON t.oid = c.conrelid JOIN pg_namespace n ON n.oid = t.relnamespace "
            + "WHERE n.nspname = ? AND c.contype IN ('p', 'u') ORDER BY t.relname, c.conname";
        try (PreparedStatement statement = connection.prepareStatement(constraintsSql)) {
            statement.setString(1, SCHEMA);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ddl.add("ALTER TABLE " + STAGING_SCHEMA + "." + resultSet.getString(1) + " ADD CONSTRAINT " + resultSet.getString(2) + " " + resultSet.getString(3));
                }
            }
        }

        // Indexes backing a primary key or a unique constraint are created by the constraints above
        String indexesSql = "SELECT pg_get_indexdef(i.indexrelid) FROM pg_index i "
            + "JOIN pg_class t ON t.oid = i.indrelid JOIN pg_namespace n ON n.oid = t.relnamespace "
            + "WHERE n.nspname = ? AND NOT EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conindid = i.indexrelid) ORDER BY 1";
        try (PreparedStatement statement = connection.prepareStatement(indexesSql)) {
            statement.setString(1, SCHEMA);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ddl.add(resultSet.getString(1).replace(" ON " + SCHEMA + ".", " ON " + STAGING_SCHEMA + "."));
                }
            }
        }

        try (Statement statement = connection.createStatement()) {
            for (String sql : ddl) {
                statement.execute(sql);
            }
        }
    }

    private void analyze(Connection connection, List<String> tables) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            for (String table : tables) {
                statement.execute("ANALYZE " + STAGING_SCHEMA + "." + table);
            }
        }
    }

    private Map<String, Long> copyFiles(Connection connection, Path zipPath) throws SQLException, IOException {
        CopyManager copyManager = connection.unwrap(PGConnection.class).getCopyAPI();
        Map<String, Set<String>> columnsByTable = this.getStagingColumns(connection);
        Map<String, Long> rowsByTable = new HashMap<>();

        try (ZipFile zipFile = new ZipFile(zipPath.toFile(), StandardCharsets.UTF_8)) {
            for (ZipEntry entry : Collections.list(zipFile.entries())) {
                String fileName = Path.of(entry.getName()).getFileName().toString().toLowerCase(Locale.ROOT);
                String table = FILE_TO_TABLE.get(fileName);

                if (table == null) {
                    log.warn("GTFS file {} ignored: no matching table", entry.getName());
                    continue;
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8))) {
                    String header = reader.readLine();
                    if (header == null) {
                        log.warn("GTFS file {} is empty", fileName);
                        continue;
                    }

                    List<String> columns = this.parseHeader(header);
                    List<String> unknownColumns = columns.stream()
                        .filter(column -> !columnsByTable.get(table).contains(column))
                        .toList();
                    if (!unknownColumns.isEmpty()) {
                        throw new IllegalStateException("GTFS file " + fileName + " has unknown columns " + unknownColumns + ", add them with a Liquibase changeset");
                    }

                    long start = System.currentTimeMillis();
                    String sql = "COPY " + STAGING_SCHEMA + "." + table + " (" + String.join(", ", columns) + ") FROM STDIN WITH (FORMAT csv)";
                    long rows = copyManager.copyIn(sql, reader);
                    rowsByTable.merge(table, rows, Long::sum);

                    log.info("GTFS {} loaded: {} rows (took {}ms)", fileName, rows, System.currentTimeMillis() - start);
                }
            }
        }

        return rowsByTable;
    }

    private List<String> parseHeader(String header) {
        return Arrays.stream(header.replace("﻿", "").split(","))
            .map(column -> column.replace("\"", "").trim().toLowerCase(Locale.ROOT))
            .map(column -> COLUMN_ALIASES.getOrDefault(column, column))
            .toList();
    }

    private Map<String, Set<String>> getStagingColumns(Connection connection) throws SQLException {
        String sql = "SELECT table_name, column_name FROM information_schema.columns WHERE table_schema = ? AND is_generated = 'NEVER'";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, STAGING_SCHEMA);
            Map<String, Set<String>> columnsByTable = new HashMap<>();
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    columnsByTable.computeIfAbsent(resultSet.getString("table_name"), k -> new HashSet<>()).add(resultSet.getString("column_name"));
                }
            }
            return columnsByTable;
        }
    }

    private void checkRows(Connection connection, Map<String, Long> rowsByTable) throws SQLException {
        List<String> emptyTables = MANDATORY_TABLES.stream()
            .filter(table -> rowsByTable.getOrDefault(table, 0L) == 0)
            .sorted()
            .toList();
        if (!emptyTables.isEmpty()) {
            throw new IllegalStateException("GTFS mandatory tables are empty: " + emptyTables);
        }

        if (rowsByTable.getOrDefault("calendar", 0L) + rowsByTable.getOrDefault("calendar_dates", 0L) == 0) {
            throw new IllegalStateException("GTFS has neither calendar nor calendar_dates");
        }

        for (String table : List.of("trips", "stop_times")) {
            long currentRows = this.countCurrentRows(connection, table);
            long newRows = rowsByTable.get(table);
            if (newRows < currentRows * MIN_ROW_RATIO) {
                throw new IllegalStateException("GTFS " + table + " looks truncated: " + newRows + " rows instead of " + currentRows);
            }
        }

        log.info("GTFS rows by table: {}", describe(rowsByTable));
    }

    private long countCurrentRows(Connection connection, String table) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT to_regclass('" + SCHEMA + "." + table + "') IS NOT NULL")) {
                resultSet.next();
                if (!resultSet.getBoolean(1)) {
                    return 0;
                }
            }
            try (ResultSet resultSet = statement.executeQuery("SELECT count(*) FROM " + SCHEMA + "." + table)) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }
}
