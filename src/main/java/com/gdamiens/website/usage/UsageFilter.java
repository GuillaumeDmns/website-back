package com.gdamiens.website.usage;

import com.gdamiens.website.security.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.PathContainer;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
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

    /** Counted together: the requests to no route of the API (scans, made-up paths), which must not each be a row */
    static final String UNKNOWN_ROUTE = "(unknown)";

    private static final Pattern HEADER_VALUE = Pattern.compile("[a-z0-9._+-]{1,24}");

    private final UsageRecorder recorder;

    /** The API's routes ({@code /api/v2/stops/{stopAreaId}/departures}), most specific first; read at the first use */
    private final Supplier<List<PathPattern>> routes;

    /**
     * @param routes the routes of the controllers, to name a request refused before reaching one
     */
    public UsageFilter(UsageRecorder recorder, Supplier<Collection<String>> routes) {
        this.recorder = recorder;
        this.routes = new Supplier<>() {
            private volatile List<PathPattern> parsed;

            @Override
            public List<PathPattern> get() {
                List<PathPattern> current = parsed;
                if (current == null) {
                    parsed = current = parse(routes.get());
                }
                return current;
            }
        };
    }

    static List<PathPattern> parse(Collection<String> routes) {
        return routes.stream().distinct().map(PathPatternParser.defaultInstance::parse).sorted(PathPattern.SPECIFICITY_COMPARATOR).toList();
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
            recorder.request(endpoint(request.getMethod(), pattern == null ? null : pattern.toString(), request.getRequestURI(), routes.get()),
                caller, response.getStatus(), millis);
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
     * {@code GET /api/v2/stops/{stopAreaId}/departures}: the route matched by Spring, else the one of [routes] the
     * path belongs to (a request refused before reaching its controller: rate limited, without token), else
     * {@link #UNKNOWN_ROUTE}. Never the path as sent: anyone can ask for as many paths as they like.
     */
    static String endpoint(String method, String pattern, String uri, List<PathPattern> routes) {
        String path = pattern != null ? pattern : route(uri, routes);
        String endpoint = method + " " + path;
        return endpoint.length() > 120 ? endpoint.substring(0, 120) : endpoint;
    }

    private static String route(String uri, List<PathPattern> routes) {
        try {
            PathContainer path = PathContainer.parsePath(uri);
            return routes.stream().filter(route -> route.matches(path)).map(PathPattern::getPatternString).findFirst().orElse(UNKNOWN_ROUTE);
        } catch (RuntimeException e) {
            return UNKNOWN_ROUTE;
        }
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
