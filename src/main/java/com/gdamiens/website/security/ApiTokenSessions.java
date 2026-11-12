package com.gdamiens.website.security;

import com.gdamiens.website.exceptions.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Access JWTs given for a personal API token ({@code POST /api/auth/api-token}) say so in a claim. They open the API
 * like the admin's own session, but not what would let a leaked API token outlive its revocation or harm the account:
 * creating more API tokens, deleting the account.
 */
public final class ApiTokenSessions {

    public static final String CLAIM = "via";

    public static final String API_TOKEN = "api-token";

    /** Stable code of the refusal, for scripts */
    public static final String REFUSED_CODE = "api_token_session";

    private ApiTokenSessions() {
    }

    public static boolean isApiTokenSession(Authentication authentication) {
        return authentication instanceof JwtAuthenticationToken jwt && API_TOKEN.equals(jwt.getToken().getClaimAsString(CLAIM));
    }

    /** @throws CustomException 403 when the caller signed in with an API token */
    public static void requireOwnSession(Authentication authentication) {
        if (isApiTokenSession(authentication)) {
            throw new CustomException("Not allowed with an API token: sign in with Google", HttpStatus.FORBIDDEN, REFUSED_CODE);
        }
    }
}
