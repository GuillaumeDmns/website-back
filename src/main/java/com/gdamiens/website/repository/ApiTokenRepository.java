package com.gdamiens.website.repository;

import com.gdamiens.website.controller.object.v2.ApiToken;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Personal API tokens ({@code public.api_token}), native SQL like {@link GtfsImportRepository}
 */
@Repository
public class ApiTokenRepository {

    private static final RowMapper<ApiToken> ROW = (rs, i) -> new ApiToken(rs.getLong("id"), rs.getString("name"),
        instant(rs.getTimestamp("created_at")), instant(rs.getTimestamp("expires_at")), instant(rs.getTimestamp("last_used_at")));

    private final NamedParameterJdbcTemplate jdbc;

    public ApiTokenRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public ApiToken create(int userId, String name, String tokenHash, Instant expiresAt) {
        return jdbc.queryForObject("""
                INSERT INTO public.api_token (user_id, name, token_hash, expires_at) VALUES (:userId, :name, :hash, :expiresAt)
                RETURNING id, name, created_at, expires_at, last_used_at""",
            new MapSqlParameterSource().addValue("userId", userId).addValue("name", name).addValue("hash", tokenHash)
                .addValue("expiresAt", Timestamp.from(expiresAt)), ROW);
    }

    public List<ApiToken> findByUser(int userId) {
        return jdbc.query("SELECT * FROM public.api_token WHERE user_id = :userId ORDER BY created_at, id",
            new MapSqlParameterSource("userId", userId), ROW);
    }

    public int countByUser(int userId) {
        Integer count = jdbc.queryForObject("SELECT count(*) FROM public.api_token WHERE user_id = :userId",
            new MapSqlParameterSource("userId", userId), Integer.class);
        return count == null ? 0 : count;
    }

    /** @return whether the token existed and belonged to the user */
    public boolean delete(int userId, long id) {
        return jdbc.update("DELETE FROM public.api_token WHERE id = :id AND user_id = :userId",
            new MapSqlParameterSource().addValue("id", id).addValue("userId", userId)) > 0;
    }

    /**
     * Marks a valid (not expired) token as used now
     *
     * @return its user's id, empty if no such valid token
     */
    public Optional<Integer> use(String tokenHash, Instant now) {
        return jdbc.query("""
                UPDATE public.api_token SET last_used_at = :now
                WHERE token_hash = :hash AND expires_at > :now
                RETURNING user_id""",
            new MapSqlParameterSource().addValue("hash", tokenHash).addValue("now", Timestamp.from(now)),
            (rs, i) -> rs.getInt(1)).stream().findFirst();
    }

    private static Instant instant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
