package com.gdamiens.website.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
  ROLE_ADMIN, ROLE_USER,
  /** A device without account (guest token, never stored): capped usage, no {@code /api/v2/me} */
  ROLE_GUEST;

  public String getAuthority() {
    return name();
  }

  /** True for a guest device's token, false for an account or no authentication (background tasks) */
  public static boolean isGuest(Authentication authentication) {
    return authentication != null && authentication.getAuthorities().stream()
        .anyMatch(authority -> ROLE_GUEST.name().equals(authority.getAuthority()));
  }
}
