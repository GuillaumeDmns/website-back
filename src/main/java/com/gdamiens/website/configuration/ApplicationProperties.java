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

    private final Quota quota = new Quota();

    private final Budget budget = new Budget();

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

    public Quota getQuota() {
        return quota;
    }

    public Budget getBudget() {
        return budget;
    }

    /**
     * Partition of the daily PRIM quotas between accounts and guest devices (see {@code ApiQuota})
     */
    public static class Quota {

        /** Share of each quota the guests can use, at most 1/16 of it per hour; the accounts keep the rest */
        private double guestShare = 0.15;

        public double getGuestShare() {
            return guestShare;
        }

        public void setGuestShare(double guestShare) {
            this.guestShare = guestShare;
        }
    }

    /**
     * Usage allowed per guest device or account, on top of the per-minute rate limit
     */
    public static class Budget {

        /** Journey searches (earlier/later pages and GO recalculations included) per guest device */
        private int guestJourneysPerDay = 10;

        private int userJourneysPerDay = 150;

        private int userJourneysPerHour = 60;

        /** Guest tokens given to an IP address (mobile networks share their addresses between many devices) */
        private int guestTokensPerIpPerHour = 30;

        public int getGuestJourneysPerDay() {
            return guestJourneysPerDay;
        }

        public void setGuestJourneysPerDay(int guestJourneysPerDay) {
            this.guestJourneysPerDay = guestJourneysPerDay;
        }

        public int getUserJourneysPerDay() {
            return userJourneysPerDay;
        }

        public void setUserJourneysPerDay(int userJourneysPerDay) {
            this.userJourneysPerDay = userJourneysPerDay;
        }

        public int getUserJourneysPerHour() {
            return userJourneysPerHour;
        }

        public void setUserJourneysPerHour(int userJourneysPerHour) {
            this.userJourneysPerHour = userJourneysPerHour;
        }

        public int getGuestTokensPerIpPerHour() {
            return guestTokensPerIpPerHour;
        }

        public void setGuestTokensPerIpPerHour(int guestTokensPerIpPerHour) {
            this.guestTokensPerIpPerHour = guestTokensPerIpPerHour;
        }
    }

    /**
     * Requests allowed per minute on {@code /api/**}, to protect the PRIM quota and the auth endpoints.
     */
    public static class RateLimit {

        private boolean enabled = true;

        /** Per IP, on sign-in / sign-up / token refresh / logout */
        private int authPerMinute = 10;

        /** Per account */
        private int userPerMinute = 120;

        /** Per guest device, and per IP on the other public endpoints */
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
