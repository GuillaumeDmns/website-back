package com.gdamiens.website.controller.object;

/** @param idToken Google ID token (OpenID Connect) obtained by the app */
public record GoogleSignInRequest(String idToken) {
}
