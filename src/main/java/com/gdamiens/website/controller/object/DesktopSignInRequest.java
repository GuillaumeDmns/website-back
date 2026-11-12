package com.gdamiens.website.controller.object;

/** @param port port the desktop app listens on, on the loopback ({@code http://127.0.0.1:<port>/}), for the end of the sign-in */
public record DesktopSignInRequest(Integer port) {
}
