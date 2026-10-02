package com.gdamiens.website.service;

import com.gdamiens.website.model.User;
import com.gdamiens.website.repository.UserRepository;
import com.gdamiens.website.security.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final JwtEncoder jwtEncoder;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Value("${security.jwt.token.expire-length:300000}")
    private long tokenValidityMs;

    public UserService(JwtEncoder jwtEncoder, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.jwtEncoder = jwtEncoder;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String signIn(String username, String password) {
        return userRepository.getByLogin(username)
            .filter(user -> passwordEncoder.matches(password, user.getPassword()))
            .map(user -> createToken(username, List.of(Role.ROLE_ADMIN)))
            .orElse(null);
    }

    public String refresh(String username) {
        return userRepository.getByLogin(username)
            .map(user -> createToken(username, List.of(Role.ROLE_ADMIN)))
            .orElse(null);
    }

    /**
     * Creates a new user with a BCrypt-hashed password.
     * @return the created User, or empty if the login already exists
     */
    public Optional<User> createUser(String login, String password) {
        if (userRepository.getByLogin(login).isPresent()) {
            return Optional.empty();
        }

        User user = new User();
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode(password));
        return Optional.of(userRepository.save(user));
    }

    private String createToken(String username, List<Role> roles) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .subject(username)
            .issuedAt(now)
            .expiresAt(now.plusMillis(tokenValidityMs))
            .claim("auth", roles.stream().map(Role::getAuthority).toList())
            .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(
            JwsHeader.with(MacAlgorithm.HS256).build(), claims
        )).getTokenValue();
    }
}
