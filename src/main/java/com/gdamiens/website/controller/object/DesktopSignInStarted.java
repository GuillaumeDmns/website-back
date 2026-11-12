package com.gdamiens.website.controller.object;

/**
 * A sign-in with Google started by a desktop app.
 *
 * @param authorizationUrl Google's page, to open in the system browser
 * @param state            comes back to the app's port with the one-time code (or the error)
 * @param verifier         secret of this sign-in, never given to the browser: sent with the one-time code
 */
public record DesktopSignInStarted(String authorizationUrl, String state, String verifier) {
}
