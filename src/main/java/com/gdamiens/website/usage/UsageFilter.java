package com.gdamiens.website.usage;

import com.gdamiens.website.security.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Counts every {@code /api/**} request for the administration ({@link UsageRecorder}): its route, the kind of caller,
 * its status and time; the account or guest device active today; the app's platform and version
 * ({@code X-App-Platform}, {@code X-App-Version} headers). Placed after the JWT authentication so that the caller is
 * known, and before the rate limiting so that its refusals are counted.
 */
public class UsageFilter extends OncePerRequestFilter {

    public static final String PLATFORM_HEADER = "X-App-Platform";

    public static final String VERSION_HEADER = "X-App-Version";

    /** A path segment that is an id: an IDFM id ({@code IDFM:71264}), a line ({@code C01371}), a number; not {@code v2} */
    private static final Pattern ID_SEGMENT = Pattern.compile(".*:.*|[A-Za-z]{0,3}[0-9]{2,}.*");

    private static final Pattern HEADER_VALUE = Pattern.compile("[a-z0-9._+-]{1,24}");

    private final UsageRecorder recorder;

    public UsageFilter(UsageRecorder recorder) {
        this.recorder = recorder;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return HttpMethod.OPTIONS.matches(request.getMethod()) || !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        long start = System.nanoTime();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UsageRecorder.Caller caller = caller(authentication);
        try {
            filterChain.doFilter(request, response);
        } finally {
            long millis = (System.nanoTime() - start) / 1_000_000;
            Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            recorder.request(endpoint(request.getMethod(), pattern == null ? null : pattern.toString(), request.getRequestURI()), caller,
                response.getStatus(), millis);
            if (caller != UsageRecorder.Caller.ANONYMOUS) {
                String identity = authentication.getName();
                recorder.member(caller == UsageRecorder.Caller.GUEST ? "active:guest" : "active:account", identity);
                String platform = headerValue(request.getHeader(PLATFORM_HEADER));
                String version = headerValue(request.getHeader(VERSION_HEADER));
                if (platform != null) {
                    recorder.member("platform:" + platform, identity);
                }
                if (version != null) {
                    recorder.member("version:" + version, identity);
                }
            }
        }
    }

    static UsageRecorder.Caller caller(Authentication authentication) {
        if (Role.isGuest(authentication)) {
            return UsageRecorder.Caller.GUEST;
        }
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return UsageRecorder.Caller.ACCOUNT;
        }
        return UsageRecorder.Caller.ANONYMOUS;
    }

    /**
     * {@code GET /api/v2/stops/{stopAreaId}/departures}: the route matched by Spring, else the path with its ids
     * replaced (a request refused before reaching its controller, e.g. rate limited)
     */
    static String endpoint(String method, String pattern, String uri) {
        String path = pattern != null ? pattern : String.join("/", java.util.Arrays.stream(uri.split("/", -1))
            .map(segment -> ID_SEGMENT.matcher(segment).matches() ? "{id}" : segment)
            .toList());
        String endpoint = method + " " + path;
        return endpoint.length() > 120 ? endpoint.substring(0, 120) : endpoint;
    }

    /** A short, plain value of the app's headers ({@code android}, {@code 0.4.0-12}), null otherwise */
    static String headerValue(String value) {
        if (value == null) {
            return null;
        }
        String clean = value.trim().toLowerCase(Locale.ROOT);
        return HEADER_VALUE.matcher(clean).matches() ? clean : null;
    }
}
