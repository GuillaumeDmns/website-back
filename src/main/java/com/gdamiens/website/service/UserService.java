package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.JwtDTO;
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
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{3,50}$");

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private static final int PASSWORD_MIN_LENGTH = 8;

    /** BCrypt ignores (and Spring rejects) anything beyond 72 bytes */
    private static final int PASSWORD_MAX_BYTES = 72;

    private final JwtEncoder jwtEncoder;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final RefreshTokenService refreshTokenService;

    @Value("${security.jwt.token.expire-length:300000}")
    private long tokenValidityMs;

    public UserService(JwtEncoder jwtEncoder, UserRepository userRepository, PasswordEncoder passwordEncoder,
                       RefreshTokenService refreshTokenService) {
        this.jwtEncoder = jwtEncoder;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
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
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .subject(user.getLogin())
            .issuedAt(now)
            .expiresAt(now.plusMillis(tokenValidityMs))
            .claim("auth", List.of(user.getRole().getAuthority()))
            .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(
            JwsHeader.with(MacAlgorithm.HS256).build(), claims
        )).getTokenValue();
    }
}
