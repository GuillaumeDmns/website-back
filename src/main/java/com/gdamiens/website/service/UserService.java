package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.controller.object.JwtDTO;
import com.gdamiens.website.controller.object.v2.Account;
import com.gdamiens.website.controller.object.v2.AccountExport;
import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.model.User;
import com.gdamiens.website.repository.UserRepository;
import com.gdamiens.website.security.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    /** A guest device keeps its token (and its budgets) this long, then gets a new one */
    private static final Duration GUEST_TOKEN_VALIDITY = Duration.ofDays(30);

    /** Guest subjects can't be logins, which have no ':' */
    private static final String GUEST_SUBJECT_PREFIX = "guest:";

    /** Login (and JWT subject) of a Google account: Google's subject, which no username can be */
    private static final String GOOGLE_LOGIN_PREFIX = "google:";

    private final JwtEncoder jwtEncoder;

    private final UserRepository userRepository;

    private final RefreshTokenService refreshTokenService;

    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    private final FavoriteService favoriteService;

    private final Set<String> adminEmails;

    @Value("${security.jwt.token.expire-length:300000}")
    private long tokenValidityMs;

    private final ApiTokenService apiTokenService;

    public UserService(JwtEncoder jwtEncoder, UserRepository userRepository, RefreshTokenService refreshTokenService,
                       GoogleIdTokenVerifier googleIdTokenVerifier, FavoriteService favoriteService, ApiTokenService apiTokenService,
                       ApplicationProperties applicationProperties) {
        this.jwtEncoder = jwtEncoder;
        this.userRepository = userRepository;
        this.apiTokenService = apiTokenService;
        this.refreshTokenService = refreshTokenService;
        this.googleIdTokenVerifier = googleIdTokenVerifier;
        this.favoriteService = favoriteService;
        this.adminEmails = applicationProperties.getAdminEmails().stream()
            .map(email -> email.trim().toLowerCase(Locale.ROOT))
            .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Signs in with a Google ID token: the account of this Google user, created at the first sign-in. Its email and
     * first name follow Google's; an email listed in {@code application.admin-emails} gets {@link Role#ROLE_ADMIN}.
     *
     * @throws CustomException 401 if the token is invalid, 503 if Google sign-in is not configured
     */
    @Transactional
    public JwtDTO signInWithGoogle(String idToken) {
        GoogleIdTokenVerifier.GoogleAccount google = googleIdTokenVerifier.verify(idToken);
        User user = userRepository.getByGoogleSub(google.sub()).orElseGet(() -> {
            User created = new User();
            created.setLogin(GOOGLE_LOGIN_PREFIX + google.sub());
            created.setGoogleSub(google.sub());
            created.setRole(Role.ROLE_USER);
            return created;
        });
        // Another account may already hold the email (Google accounts sharing an address): this one then keeps none
        if (google.email() != null && !google.email().equalsIgnoreCase(user.getEmail())) {
            boolean taken = userRepository.getByEmailIgnoreCase(google.email())
                .filter(other -> !Objects.equals(other.getId(), user.getId()))
                .isPresent();
            if (!taken) {
                user.setEmail(google.email());
            }
        }
        user.setDisplayName(google.firstName());
        user.setLastLoginAt(OffsetDateTime.now());
        if (google.email() != null && adminEmails.contains(google.email().toLowerCase(Locale.ROOT))) {
            user.setRole(Role.ROLE_ADMIN);
        }
        return createTokens(userRepository.save(user));
    }

    public Account getAccount(String login) {
        return toAccount(user(login));
    }

    /** The account and its favorites, as kept on the server */
    public AccountExport exportAccount(String login) {
        return new AccountExport(Instant.now(), getAccount(login), favoriteService.list(login));
    }

    /** Deletes the account for good, with its favorites and refresh tokens (database cascades) */
    @Transactional
    public void deleteAccount(String login) {
        userRepository.delete(user(login));
    }

    private static Account toAccount(User user) {
        return new Account(user.getEmail(), user.getDisplayName(), user.getRole().name(), user.getGoogleSub() != null,
            user.getCreatedAt());
    }

    /**
     * Rotates a refresh token.
     * @return a new access token and refresh token, or empty if the refresh token is invalid
     */
    @Transactional
    public Optional<JwtDTO> refresh(String refreshToken) {
        return refreshTokenService.consume(refreshToken).map(this::createTokens);
    }

    /**
     * Access token of a device used without account: a random subject with {@link Role#ROLE_GUEST}, no refresh token
     */
    public JwtDTO createGuestToken() {
        String token = createJwt(GUEST_SUBJECT_PREFIX + UUID.randomUUID(), Role.ROLE_GUEST, GUEST_TOKEN_VALIDITY);
        return new JwtDTO(token, null, GUEST_TOKEN_VALIDITY.toSeconds());
    }

    public void signOut(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    /**
     * Access token of the account of a personal API token (Bruno, scripts): no refresh token, the API token is used
     * again when it expires
     *
     * @return empty if the API token is unknown or expired
     */
    @Transactional
    public Optional<JwtDTO> signInWithApiToken(String apiToken) {
        return apiTokenService.use(apiToken)
            .flatMap(userRepository::findById)
            .map(user -> new JwtDTO(createAccessToken(user), null, tokenValidityMs / 1000));
    }

    /**
     * @throws CustomException 401 when the account no longer exists
     */
    public User user(String login) {
        return Optional.ofNullable(login)
            .flatMap(userRepository::getByLoginIgnoreCase)
            .orElseThrow(() -> new CustomException("Unknown user", HttpStatus.UNAUTHORIZED));
    }

    private JwtDTO createTokens(User user) {
        return new JwtDTO(createAccessToken(user), refreshTokenService.issue(user), tokenValidityMs / 1000);
    }

    private String createAccessToken(User user) {
        return createJwt(user.getLogin(), user.getRole(), Duration.ofMillis(tokenValidityMs));
    }

    private String createJwt(String subject, Role role, Duration validity) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .subject(subject)
            .issuedAt(now)
            .expiresAt(now.plus(validity))
            .claim("auth", List.of(role.getAuthority()))
            .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(
            JwsHeader.with(MacAlgorithm.HS256).build(), claims
        )).getTokenValue();
    }
}
