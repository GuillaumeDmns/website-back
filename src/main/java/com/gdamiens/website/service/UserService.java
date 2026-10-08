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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{3,50}$");

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private static final int PASSWORD_MIN_LENGTH = 8;

    /** BCrypt ignores (and Spring rejects) anything beyond 72 bytes */
    private static final int PASSWORD_MAX_BYTES = 72;

    /** A guest device keeps its token (and its budgets) this long, then gets a new one */
    private static final Duration GUEST_TOKEN_VALIDITY = Duration.ofDays(30);

    /** Guest subjects can't be logins, which have no ':' */
    private static final String GUEST_SUBJECT_PREFIX = "guest:";

    /** Login (and JWT subject) of a Google account: Google's subject, which no username can be */
    private static final String GOOGLE_LOGIN_PREFIX = "google:";

    private final JwtEncoder jwtEncoder;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final RefreshTokenService refreshTokenService;

    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    private final FavoriteService favoriteService;

    private final Set<String> adminEmails;

    @Value("${security.jwt.token.expire-length:300000}")
    private long tokenValidityMs;

    public UserService(JwtEncoder jwtEncoder, UserRepository userRepository, PasswordEncoder passwordEncoder,
                       RefreshTokenService refreshTokenService, GoogleIdTokenVerifier googleIdTokenVerifier,
                       FavoriteService favoriteService, ApplicationProperties applicationProperties) {
        this.jwtEncoder = jwtEncoder;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        // A password account may already hold the email: the Google account then keeps none (no merge: that address
        // was never verified for the password account)
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

    private User user(String login) {
        return Optional.ofNullable(login)
            .flatMap(userRepository::getByLoginIgnoreCase)
            .orElseThrow(() -> new CustomException("Unknown user", HttpStatus.UNAUTHORIZED));
    }

    private static Account toAccount(User user) {
        return new Account(user.getEmail(), user.getDisplayName(), user.getRole().name(), user.getGoogleSub() != null,
            user.getCreatedAt());
    }

    /**
     * @return an access token and a refresh token, or empty if the credentials are invalid
     */
    @Transactional
    public Optional<JwtDTO> signIn(String username, String password) {
        return userRepository.getByLoginIgnoreCase(username)
            .filter(user -> passwordEncoder.matches(password, user.getPassword()))
            .map(this::createTokens);
    }

    /**
     * Creates a {@link Role#ROLE_USER} account and signs it in.
     * @throws CustomException 400 if a field is invalid, 409 if the username or email is already used
     */
    @Transactional
    public JwtDTO signUp(String username, String email, String password) {
        if (username == null || !USERNAME_PATTERN.matcher(username).matches()) {
            throw new CustomException("Username must be 3 to 50 letters, digits, '.', '_' or '-'", HttpStatus.BAD_REQUEST);
        }
        if (email == null || email.length() > 255 || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new CustomException("Invalid email", HttpStatus.BAD_REQUEST);
        }
        validatePassword(password);
        if (userRepository.getByEmailIgnoreCase(email).isPresent()) {
            throw new CustomException("Email already used", HttpStatus.CONFLICT);
        }

        User user = createUser(username, password, Role.ROLE_USER)
            .orElseThrow(() -> new CustomException("Username already used", HttpStatus.CONFLICT));
        user.setEmail(email);

        return createTokens(user);
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
     * Creates a new user with a BCrypt-hashed password.
     * @return the created User, or empty if the login already exists
     */
    private Optional<User> createUser(String login, String password, Role role) {
        if (userRepository.getByLoginIgnoreCase(login).isPresent()) {
            return Optional.empty();
        }

        User user = new User();
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        return Optional.of(userRepository.save(user));
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < PASSWORD_MIN_LENGTH) {
            throw new CustomException("Password must be at least " + PASSWORD_MIN_LENGTH + " characters", HttpStatus.BAD_REQUEST);
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > PASSWORD_MAX_BYTES) {
            throw new CustomException("Password is too long", HttpStatus.BAD_REQUEST);
        }
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
