package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.controller.object.JwtDTO;
import com.gdamiens.website.model.User;
import com.gdamiens.website.repository.UserRepository;
import com.gdamiens.website.security.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** The admin role follows application.admin-emails each time tokens are given */
class UserServiceTest {

    private final JwtEncoder jwtEncoder = mock(JwtEncoder.class);

    private final UserRepository userRepository = mock(UserRepository.class);

    private final RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);

    private final ApiTokenService apiTokenService = mock(ApiTokenService.class);

    private UserService userService;

    @BeforeEach
    void setUp() {
        ApplicationProperties properties = new ApplicationProperties();
        properties.setAdminEmails(List.of("Admin@example.com"));
        userService = new UserService(jwtEncoder, userRepository, refreshTokenService, mock(GoogleIdTokenVerifier.class),
            mock(FavoriteService.class), apiTokenService, properties);
        when(jwtEncoder.encode(any())).thenReturn(Jwt.withTokenValue("jwt").header("alg", "HS256").subject("someone").build());
        when(userRepository.save(any())).thenAnswer(saved -> saved.getArgument(0));
    }

    @Test
    void anAdminTakenOffTheListIsOneNoMoreAtItsNextRefresh() {
        User former = user("former@example.com", Role.ROLE_ADMIN);
        when(refreshTokenService.consume("refresh")).thenReturn(Optional.of(former));

        Optional<JwtDTO> tokens = userService.refresh("refresh");

        assertThat(tokens).isPresent();
        assertThat(former.getRole()).isEqualTo(Role.ROLE_USER);
        verify(userRepository).save(former);
        assertThat(roleOfTheTokenGiven()).containsExactly("ROLE_USER");
    }

    @Test
    void anAdminStillListedStaysOneWhateverTheCase() {
        User admin = user("admin@example.com", Role.ROLE_ADMIN);
        when(refreshTokenService.consume("refresh")).thenReturn(Optional.of(admin));

        userService.refresh("refresh");

        assertThat(admin.getRole()).isEqualTo(Role.ROLE_ADMIN);
        verify(userRepository, never()).save(any());
        assertThat(roleOfTheTokenGiven()).containsExactly("ROLE_ADMIN");
    }

    @Test
    void theApiTokenOfAFormerAdminOpensNothing() {
        User former = user("former@example.com", Role.ROLE_ADMIN);
        when(apiTokenService.use("gdpat_left_behind")).thenReturn(Optional.of(7));
        when(userRepository.findById(7)).thenReturn(Optional.of(former));

        assertThat(userService.signInWithApiToken("gdpat_left_behind")).isEmpty();
        assertThat(former.getRole()).isEqualTo(Role.ROLE_USER);
        verify(jwtEncoder, never()).encode(any());
    }

    @Test
    void theApiTokenOfAnAdminGivesItsAccessToken() {
        when(apiTokenService.use("gdpat_bruno")).thenReturn(Optional.of(1));
        when(userRepository.findById(1)).thenReturn(Optional.of(user("admin@example.com", Role.ROLE_ADMIN)));

        assertThat(userService.signInWithApiToken("gdpat_bruno")).isPresent();
        assertThat(roleOfTheTokenGiven()).containsExactly("ROLE_ADMIN");
    }

    private List<String> roleOfTheTokenGiven() {
        ArgumentCaptor<JwtEncoderParameters> given = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(given.capture());
        return given.getValue().getClaims().getClaimAsStringList("auth");
    }

    private static User user(String email, Role role) {
        User user = new User();
        user.setLogin("google:" + email);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }
}
