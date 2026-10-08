package com.gdamiens.website.repository;

import com.gdamiens.website.model.GtfsImport;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * History of the GTFS imports ({@code public.gtfs_import}), native SQL like {@link NetworkRepository}
 */
@Repository
public class GtfsImportRepository {

    private static final String COLUMNS = "id, source, feed_date, started_at, finished_at, status, row_counts, error";

    private static final RowMapper<GtfsImport> ROW = (rs, i) -> new GtfsImport(rs.getLong(1), rs.getString(2),
        instant(rs.getTimestamp(3)), instant(rs.getTimestamp(4)), instant(rs.getTimestamp(5)), rs.getString(6),
        rs.getString(7), rs.getString(8));

    private final NamedParameterJdbcTemplate jdbc;

    public GtfsImportRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** @return the id of the import, {@link GtfsImport#RUNNING} */
    public long start(String source, Instant feedDate) {
        return jdbc.queryForObject("""
                INSERT INTO public.gtfs_import (source, feed_date, status) VALUES (:source, :feedDate, 'RUNNING')
                RETURNING id""",
            new MapSqlParameterSource().addValue("source", source).addValue("feedDate", timestamp(feedDate)), Long.class);
    }

    public void finish(long id, String status, String rowCounts, String error) {
        jdbc.update("""
                UPDATE public.gtfs_import SET finished_at = now(), status = :status, row_counts = :rowCounts, error = :error
                WHERE id = :id""",
            new MapSqlParameterSource().addValue("id", id).addValue("status", status)
                .addValue("rowCounts", rowCounts).addValue("error", error));
    }

    /** Imports left running by a stopped application */
    public int markInterrupted() {
        return jdbc.update("""
                UPDATE public.gtfs_import SET finished_at = now(), status = 'FAILED', error = 'Interrupted (application stopped)'
                WHERE status = 'RUNNING'""", new MapSqlParameterSource());
    }

    public Optional<GtfsImport> findLast() {
        return jdbc.query("SELECT %s FROM public.gtfs_import ORDER BY started_at DESC, id DESC LIMIT 1".formatted(COLUMNS), ROW)
            .stream().findFirst();
    }

    public Optional<GtfsImport> findLastSuccess() {
        return jdbc.query("SELECT %s FROM public.gtfs_import WHERE status = 'SUCCESS' ORDER BY started_at DESC, id DESC LIMIT 1".formatted(COLUMNS), ROW)
            .stream().findFirst();
    }

    public List<GtfsImport> findRecent(int limit) {
        return jdbc.query("SELECT %s FROM public.gtfs_import ORDER BY started_at DESC, id DESC LIMIT :limit".formatted(COLUMNS),
            new MapSqlParameterSource("limit", limit), ROW);
    }

    private static Instant instant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    private static Timestamp timestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }
}
