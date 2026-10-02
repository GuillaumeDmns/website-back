package com.gdamiens.website.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
@Component
public class ApplicationProperties {
    private String idfmKey;

    private String idfmStaticKey;

    /**
     * Origin patterns allowed by CORS (e.g. {@code https://guillaumedamiens.com}, {@code http://localhost:[*]}).
     */
    private List<String> corsAllowedOrigins = new ArrayList<>();

    private final RateLimit rateLimit = new RateLimit();

    public String getIdfmKey() {
        return idfmKey;
    }

    public void setIdfmKey(String idfmKey) {
        this.idfmKey = idfmKey;
    }

    public String getIdfmStaticKey() {
        return idfmStaticKey;
    }

    public void setIdfmStaticKey(String idfmStaticKey) {
        this.idfmStaticKey = idfmStaticKey;
    }

    public List<String> getCorsAllowedOrigins() {
        return corsAllowedOrigins;
    }

    public void setCorsAllowedOrigins(List<String> corsAllowedOrigins) {
        this.corsAllowedOrigins = corsAllowedOrigins;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    /**
     * Requests allowed per minute on {@code /api/**}, to protect the PRIM quota and the auth endpoints.
     */
    public static class RateLimit {

        private boolean enabled = true;

        /** Per IP, on sign-in / sign-up / token refresh / logout */
        private int authPerMinute = 10;

        /** Per authenticated user */
        private int userPerMinute = 120;

        /** Per IP, on other public endpoints */
        private int anonymousPerMinute = 60;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getAuthPerMinute() {
            return authPerMinute;
        }

        public void setAuthPerMinute(int authPerMinute) {
            this.authPerMinute = authPerMinute;
        }

        public int getUserPerMinute() {
            return userPerMinute;
        }

        public void setUserPerMinute(int userPerMinute) {
            this.userPerMinute = userPerMinute;
        }

        public int getAnonymousPerMinute() {
            return anonymousPerMinute;
        }

        public void setAnonymousPerMinute(int anonymousPerMinute) {
            this.anonymousPerMinute = anonymousPerMinute;
        }
    }
}
