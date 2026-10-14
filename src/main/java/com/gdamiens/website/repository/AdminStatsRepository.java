package com.gdamiens.website.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Anonymous counts for the administration: accounts, sessions, favorites, PRIM calls per day, GTFS and database sizes.
 * Only aggregates leave this class, never an account (no id, email or name), and never a position (home, work and saved
 * places are only counted). Days are Paris days.
 */
@Repository
public class AdminStatsRepository {

    /** Count of a Paris day */
    public record DayCount(LocalDate day, int count) {}

    /** Count of a named group (a role, a kind of favorite, a range...) */
    public record GroupCount(String group, int count) {}

    public record AccountCounts(int total, int admins, int activeToday, int active7Days, int active30Days, int new7Days,
                                int new30Days, int withSession, int apiTokens, int apiTokensUsed7Days) {}

    public record QuotaDay(LocalDate day, String feature, String api, int calls, int guestCalls) {}

    public record FavoriteTarget(String id, int count) {}

    public record TableSize(String schema, String name, long bytes, long rows) {}

    public record DatabaseInfo(long bytes, String postgres, String postgis, int connections) {}

    public record Calendar(LocalDate from, LocalDate until) {}

    /** API requests of an hour (UTC), all routes */
    public record HourRequests(java.time.Instant hour, int requests, int clientErrors, int serverErrors, int rateLimited, long totalMillis) {}

    /** A route over a period */
    public record EndpointRequests(String endpoint, int requests, int clientErrors, int serverErrors, int rateLimited, long totalMillis,
                                   long maxMillis) {}

    /** Requests of a kind of caller (ACCOUNT, GUEST, ANONYMOUS) on a Paris day */
    public record CallerRequests(LocalDate day, String caller, int requests) {}

    /** Distinct members of a metric on a day (active:account, platform:android, visitors:guillaumedamiens.com…) */
    public record MetricCount(LocalDate day, String metric, int count) {}

    public record PageViews(String host, String path, int views) {}

    public record HostViews(LocalDate day, String host, int views) {}

    public record ReferrerViews(String host, String referrer, int views) {}

    private static final String PARIS_DAY = "(%s AT TIME ZONE 'Europe/Paris')::date";

    private final NamedParameterJdbcTemplate jdbc;

    public AdminStatsRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public AccountCounts accountCounts(LocalDate today) {
        MapSqlParameterSource params = new MapSqlParameterSource("today", today);
        return jdbc.queryForObject("""
            SELECT count(*) AS total,
                   count(*) FILTER (WHERE role = 'ROLE_ADMIN') AS admins,
                   count(*) FILTER (WHERE %1$s >= :today) AS active_today,
                   count(*) FILTER (WHERE %1$s > :today - 7) AS active_7,
                   count(*) FILTER (WHERE %1$s > :today - 30) AS active_30,
                   count(*) FILTER (WHERE %2$s > :today - 7) AS new_7,
                   count(*) FILTER (WHERE %2$s > :today - 30) AS new_30,
                   (SELECT count(DISTINCT user_id) FROM public.refresh_token WHERE revoked_at IS NULL AND expires_at > now()) AS with_session,
                   (SELECT count(*) FROM public.api_token WHERE expires_at > now()) AS api_tokens,
                   (SELECT count(*) FROM public.api_token WHERE expires_at > now() AND %3$s > :today - 7) AS api_tokens_used
            FROM public."user"
            """.formatted(PARIS_DAY.formatted("last_login_at"), PARIS_DAY.formatted("created_at"), PARIS_DAY.formatted("last_used_at")),
            params, (rs, row) -> new AccountCounts(rs.getInt("total"), rs.getInt("admins"), rs.getInt("active_today"), rs.getInt("active_7"),
                rs.getInt("active_30"), rs.getInt("new_7"), rs.getInt("new_30"), rs.getInt("with_session"), rs.getInt("api_tokens"),
                rs.getInt("api_tokens_used")));
    }

    /** Accounts created per day since [from] (days without one left out) */
    public List<DayCount> signUpsPerDay(LocalDate from) {
        return perDay("public.\"user\"", "created_at", "count(*)", from);
    }

    /**
     * Accounts that used the app per day since [from]: a session is renewed every hour of use (refresh token rotated),
     * and the rotated tokens are kept 7 days, so only the last 7 days are complete
     */
    public List<DayCount> activeAccountsPerDay(LocalDate from) {
        return perDay("public.refresh_token", "created_at", "count(DISTINCT user_id)", from);
    }

    public List<DayCount> favoritesPerDay(LocalDate from) {
        return perDay("public.favorite", "created_at", "count(*)", from);
    }

    private List<DayCount> perDay(String table, String column, String count, LocalDate from) {
        String day = PARIS_DAY.formatted(column);
        return jdbc.query("SELECT %s AS day, %s AS count FROM %s WHERE %s >= :from GROUP BY 1 ORDER BY 1".formatted(day, count, table, day),
            new MapSqlParameterSource("from", from), (rs, row) -> new DayCount(rs.getObject("day", LocalDate.class), rs.getInt("count")));
    }

    /** Accounts per time since their last sign-in */
    public List<GroupCount> lastSignIns(LocalDate today) {
        String day = PARIS_DAY.formatted("last_login_at");
        return groups("""
            SELECT CASE WHEN last_login_at IS NULL THEN 'never'
                        WHEN %1$s >= :today THEN 'today'
                        WHEN %1$s > :today - 7 THEN '7d'
                        WHEN %1$s > :today - 30 THEN '30d'
                        WHEN %1$s > :today - 90 THEN '90d'
                        ELSE 'older' END AS grp,
                   count(*) AS count
            FROM public."user" GROUP BY 1
            """.formatted(day), new MapSqlParameterSource("today", today));
    }

    /** Accounts per number of favorites (0, 1, 2-4, 5-9, 10+) */
    public List<GroupCount> favoritesPerAccount() {
        return groups("""
            SELECT CASE WHEN n = 0 THEN '0' WHEN n = 1 THEN '1' WHEN n < 5 THEN '2-4' WHEN n < 10 THEN '5-9' ELSE '10+' END AS grp,
                   count(*) AS count
            FROM (SELECT u.id, count(f.id) AS n FROM public."user" u LEFT JOIN public.favorite f ON f.user_id = u.id GROUP BY u.id) per_account
            GROUP BY 1
            """, new MapSqlParameterSource());
    }

    public List<GroupCount> favoritesPerKind() {
        return groups("SELECT kind AS grp, count(*) AS count FROM public.favorite GROUP BY 1", new MapSqlParameterSource());
    }

    public int accountsWithFavorites() {
        Integer count = jdbc.queryForObject("SELECT count(DISTINCT user_id) FROM public.favorite", new MapSqlParameterSource(), Integer.class);
        return count == null ? 0 : count;
    }

    /** Stop areas saved by the most accounts */
    public List<FavoriteTarget> topFavoriteStops(int limit) {
        return topFavorites("stop_area_id", "STOP", limit);
    }

    /** Lines saved by the most accounts */
    public List<FavoriteTarget> topFavoriteLines(int limit) {
        return topFavorites("line_id", "LINE", limit);
    }

    private List<FavoriteTarget> topFavorites(String column, String kind, int limit) {
        return jdbc.query("""
            SELECT %1$s AS target, count(*) AS count FROM public.favorite WHERE kind = :kind AND %1$s IS NOT NULL
            GROUP BY 1 ORDER BY 2 DESC, 1 LIMIT :limit
            """.formatted(column), new MapSqlParameterSource("kind", kind).addValue("limit", limit),
            (rs, row) -> new FavoriteTarget(rs.getString("target"), rs.getInt("count")));
    }

    private List<GroupCount> groups(String sql, MapSqlParameterSource params) {
        return jdbc.query(sql, params, (rs, row) -> new GroupCount(rs.getString("grp"), rs.getInt("count")));
    }

    /** PRIM calls per day and feature since [from], as saved every 30 s by {@code ApiQuotaStore} */
    public List<QuotaDay> quotaDays(LocalDate from) {
        return jdbc.query("SELECT day, feature, api, calls, guest_calls FROM public.api_quota_usage WHERE day >= :from ORDER BY day, feature",
            new MapSqlParameterSource("from", from), (rs, row) -> new QuotaDay(rs.getObject("day", LocalDate.class), rs.getString("feature"),
                rs.getString("api"), rs.getInt("calls"), rs.getInt("guest_calls")));
    }

    /** Stop areas ({@code location_type} 1) and quays (0 or empty) of the GTFS */
    public Map<String, Integer> stopCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        jdbc.query("""
            SELECT CASE WHEN location_type = 1 THEN 'stopAreas' WHEN coalesce(location_type, 0) = 0 THEN 'quays' ELSE 'other' END AS grp,
                   count(*) AS count
            FROM gtfs.stops GROUP BY 1
            """, new MapSqlParameterSource(), rs -> {
            counts.put(rs.getString("grp"), rs.getInt("count"));
        });
        return counts;
    }

    /** First and last day of the services of the GTFS (calendars and their exceptions) */
    public Calendar calendar() {
        return jdbc.queryForObject("""
            SELECT least((SELECT min(start_date) FROM gtfs.calendar), (SELECT min(date) FROM gtfs.calendar_dates)) AS from_day,
                   greatest((SELECT max(end_date) FROM gtfs.calendar), (SELECT max(date) FROM gtfs.calendar_dates)) AS until_day
            """, new MapSqlParameterSource(), (rs, row) -> new Calendar(rs.getObject("from_day", LocalDate.class),
            rs.getObject("until_day", LocalDate.class)));
    }

    /** Tables of the application (public and gtfs schemas), biggest first, rows as estimated by PostgreSQL's statistics */
    public List<TableSize> tableSizes() {
        return jdbc.query("""
            SELECT n.nspname AS schema, c.relname AS name, pg_total_relation_size(c.oid) AS bytes, greatest(c.reltuples, 0)::bigint AS rows
            FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
            WHERE c.relkind IN ('r', 'p') AND n.nspname IN ('public', 'gtfs')
            ORDER BY 3 DESC
            """, new MapSqlParameterSource(), (rs, row) -> new TableSize(rs.getString("schema"), rs.getString("name"), rs.getLong("bytes"),
            rs.getLong("rows")));
    }

    public DatabaseInfo database() {
        return jdbc.queryForObject("""
            SELECT pg_database_size(current_database()) AS bytes,
                   current_setting('server_version') AS postgres,
                   (SELECT extversion FROM pg_extension WHERE extname = 'postgis') AS postgis,
                   (SELECT count(*) FROM pg_stat_activity WHERE datname = current_database()) AS connections
            """, new MapSqlParameterSource(), (rs, row) -> new DatabaseInfo(rs.getLong("bytes"), rs.getString("postgres"),
            rs.getString("postgis"), rs.getInt("connections")));
    }

    public List<HourRequests> requestsPerHour(java.time.Instant from) {
        return jdbc.query("""
            SELECT hour, sum(requests) AS requests,
                   coalesce(sum(requests) FILTER (WHERE status = '4xx'), 0) AS client_errors,
                   coalesce(sum(requests) FILTER (WHERE status = '5xx'), 0) AS server_errors,
                   coalesce(sum(requests) FILTER (WHERE status = '429'), 0) AS rate_limited,
                   sum(total_millis) AS total_millis
            FROM public.usage_hourly WHERE hour >= :from GROUP BY hour ORDER BY hour
            """, new MapSqlParameterSource("from", java.sql.Timestamp.from(from)), (rs, row) -> new HourRequests(rs.getTimestamp("hour").toInstant(),
            rs.getInt("requests"), rs.getInt("client_errors"), rs.getInt("server_errors"), rs.getInt("rate_limited"), rs.getLong("total_millis")));
    }

    public List<EndpointRequests> endpoints(java.time.Instant from, int limit) {
        return jdbc.query("""
            SELECT endpoint, sum(requests) AS requests,
                   coalesce(sum(requests) FILTER (WHERE status = '4xx'), 0) AS client_errors,
                   coalesce(sum(requests) FILTER (WHERE status = '5xx'), 0) AS server_errors,
                   coalesce(sum(requests) FILTER (WHERE status = '429'), 0) AS rate_limited,
                   sum(total_millis) AS total_millis, max(max_millis) AS max_millis
            FROM public.usage_hourly WHERE hour >= :from GROUP BY endpoint ORDER BY requests DESC LIMIT :limit
            """, new MapSqlParameterSource("from", java.sql.Timestamp.from(from)).addValue("limit", limit),
            (rs, row) -> new EndpointRequests(rs.getString("endpoint"), rs.getInt("requests"), rs.getInt("client_errors"),
                rs.getInt("server_errors"), rs.getInt("rate_limited"), rs.getLong("total_millis"), rs.getLong("max_millis")));
    }

    public List<CallerRequests> callersPerDay(LocalDate from) {
        String day = PARIS_DAY.formatted("hour");
        return jdbc.query("""
            SELECT %1$s AS day, caller, sum(requests) AS requests FROM public.usage_hourly WHERE %1$s >= :from
            GROUP BY 1, 2 ORDER BY 1, 2
            """.formatted(day), new MapSqlParameterSource("from", from),
            (rs, row) -> new CallerRequests(rs.getObject("day", LocalDate.class), rs.getString("caller"), rs.getInt("requests")));
    }

    /** Distinct counts per day and metric: the closed days, then today's (and yesterday's until closed) from their hashes */
    public List<MetricCount> metricsPerDay(LocalDate from) {
        return jdbc.query("""
            SELECT day, metric, count FROM public.usage_daily WHERE day >= :from
            UNION ALL
            SELECT day, metric, count(*) FROM public.usage_member WHERE day >= :from GROUP BY day, metric
            ORDER BY 1, 2
            """, new MapSqlParameterSource("from", from),
            (rs, row) -> new MetricCount(rs.getObject("day", LocalDate.class), rs.getString("metric"), rs.getInt("count")));
    }

    public List<HostViews> viewsPerDay(LocalDate from) {
        return jdbc.query("SELECT day, host, sum(views) AS views FROM public.usage_page_daily WHERE day >= :from GROUP BY 1, 2 ORDER BY 1, 2",
            new MapSqlParameterSource("from", from),
            (rs, row) -> new HostViews(rs.getObject("day", LocalDate.class), rs.getString("host"), rs.getInt("views")));
    }

    public List<PageViews> topPages(LocalDate from, int limit) {
        return jdbc.query("""
            SELECT host, path, sum(views) AS views FROM public.usage_page_daily WHERE day >= :from
            GROUP BY 1, 2 ORDER BY 3 DESC, 1, 2 LIMIT :limit
            """, new MapSqlParameterSource("from", from).addValue("limit", limit),
            (rs, row) -> new PageViews(rs.getString("host"), rs.getString("path"), rs.getInt("views")));
    }

    public List<ReferrerViews> topReferrers(LocalDate from, int limit) {
        return jdbc.query("""
            SELECT host, referrer, sum(views) AS views FROM public.usage_referrer_daily WHERE day >= :from
            GROUP BY 1, 2 ORDER BY 3 DESC, 1, 2 LIMIT :limit
            """, new MapSqlParameterSource("from", from).addValue("limit", limit),
            (rs, row) -> new ReferrerViews(rs.getString("host"), rs.getString("referrer"), rs.getInt("views")));
    }

    /** When the page log was last read, null before */
    public java.time.Instant pagesReadAt() {
        return jdbc.query("SELECT updated_at FROM public.usage_log_cursor WHERE name = 'pages'", new MapSqlParameterSource(),
            rs -> rs.next() ? rs.getTimestamp(1).toInstant() : null);
    }
}
