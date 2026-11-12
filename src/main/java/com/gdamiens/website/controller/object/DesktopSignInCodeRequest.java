package com.gdamiens.website.controller.object;

/**
 * @param code     one-time code the browser brought to the app's port
 * @param verifier secret given to the app when the sign-in started
 */
public record DesktopSignInCodeRequest(String code, String verifier) {
}
