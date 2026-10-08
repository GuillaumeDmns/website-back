package com.gdamiens.website.controller.object.v2;

import java.time.OffsetDateTime;

/**
 * The signed-in user's account
 *
 * @param firstName first name given by Google, null for a password account
 * @param google    signed in with Google
 */
public record Account(String email, String firstName, String role, boolean google, OffsetDateTime createdAt) {
}
