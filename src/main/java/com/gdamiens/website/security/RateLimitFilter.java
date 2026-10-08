package com.gdamiens.website.security;

import com.gdamiens.website.configuration.ApplicationProperties;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Limits {@code /api/**} requests per minute: per IP on the auth endpoints (brute force), per account or guest device
 * once authenticated, per IP otherwise. Runs after the JWT authentication so the caller is known. Answers 429 with
 * {@code Retry-After}.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> AUTH_PATHS = Set.of("/api/signin", "/api/signup", "/api/auth/guest", "/api/auth/google", "/api/token/refresh", "/api/logout");

    private final RateLimiter rateLimiter;

    private final ApplicationProperties.RateLimit limits;

    public RateLimitFilter(RateLimiter rateLimiter, ApplicationProperties.RateLimit limits) {
        this.rateLimiter = rateLimiter;
        this.limits = limits;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !limits.isEnabled()
            || HttpMethod.OPTIONS.matches(request.getMethod())
            || !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String key;
        int perMinute;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (AUTH_PATHS.contains(request.getRequestURI())) {
            key = "auth:" + request.getRemoteAddr();
            perMinute = limits.getAuthPerMinute();
        } else if (Role.isGuest(authentication)) {
            // The subject is already "guest:<id>"
            key = authentication.getName();
            perMinute = limits.getAnonymousPerMinute();
        } else if (authentication != null && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken)) {
            key = "user:" + authentication.getName();
            perMinute = limits.getUserPerMinute();
        } else {
            key = "ip:" + request.getRemoteAddr();
            perMinute = limits.getAnonymousPerMinute();
        }

        ConsumptionProbe probe = rateLimiter.tryConsume(key, perMinute);
        if (probe.isConsumed()) {
            response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write("{\"title\":\"Too Many Requests\",\"status\":429,\"detail\":\"Rate limit exceeded, retry in "
            + retryAfterSeconds + " s\",\"instance\":\"" + request.getRequestURI().replace("\"", "") + "\"}");
    }
}
