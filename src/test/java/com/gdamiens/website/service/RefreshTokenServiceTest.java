package com.gdamiens.website.service;

import com.gdamiens.website.model.RefreshToken;
import com.gdamiens.website.model.User;
import com.gdamiens.website.repository.RefreshTokenRepository;
import com.gdamiens.website.security.SecurityEvents;
import com.gdamiens.website.utils.OpaqueTokens;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshTokenServiceTest {

    private final RefreshTokenRepository repository = mock(RefreshTokenRepository.class);

    private final SecurityEvents events = new SecurityEvents(Clock.systemUTC());

    private final RefreshTokenService service = new RefreshTokenService(repository, events);

    private final User user = mock(User.class);

    private RefreshToken stored() {
        RefreshToken token = mock(RefreshToken.class);
        when(token.getId()).thenReturn(12L);
        when(token.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(3);
        when(repository.findByTokenHash(OpaqueTokens.hash("raw"))).thenReturn(Optional.of(token));
        return token;
    }

    @Test
    void aTokenIsRevokedInTheDatabaseAsItIsUsed() {
        stored();
        when(repository.revokeIfActive(eq(12L), any(OffsetDateTime.class))).thenReturn(1);

        assertThat(service.consume("raw")).contains(user);
        verify(repository, never()).revokeAllForUser(any(), any());
        assertThat(reuses()).isZero();
    }

    @Test
    void ofTwoRequestsUsingATokenAtOnceTheSecondIsAReuse() {
        // Both read it valid; the database revokes it once only
        stored();
        when(repository.revokeIfActive(eq(12L), any(OffsetDateTime.class))).thenReturn(0);

        assertThat(service.consume("raw")).isEmpty();
        verify(repository).revokeAllForUser(eq(3), any(OffsetDateTime.class));
    }

    @Test
    void aRevokedTokenUsedAgainRevokesThemAll() {
        RefreshToken token = stored();
        when(token.isRevoked()).thenReturn(true);

        assertThat(service.consume("raw")).isEmpty();
        verify(repository).revokeAllForUser(eq(3), any(OffsetDateTime.class));
        verify(repository, never()).revokeIfActive(any(), any());
        // The administration's security page shows it
        assertThat(reuses()).isEqualTo(1);
    }

    private long reuses() {
        return events.counts().stream().filter(count -> count.kind() == SecurityEvents.Kind.REFRESH_TOKEN_REUSED)
            .findFirst().orElseThrow().today();
    }
}
