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

    private final Gtfs gtfs = new Gtfs();

    private final Google google = new Google();

    private final Urls urls = new Urls();

    private final Scheduling scheduling = new Scheduling();

    /** Verified emails (Google) given {@code ROLE_ADMIN} when they sign in */
    private List<String> adminEmails = new ArrayList<>();

    /** Pages checked for the administration (status, response time, TLS certificate) */
    private List<String> monitoredSites = new ArrayList<>();

    /**
     * Log of the page loads of the static sites written by nginx (format {@code guillaumedamiens_pages} of
     * {@code deploy/nginx/guillaumedamiens.conf}), read for the visit counts; empty: not read
     */
    private String pagesLog = "";

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

    public Gtfs getGtfs() {
        return gtfs;
    }

    public Google getGoogle() {
        return google;
    }

    public Urls getUrls() {
        return urls;
    }

    public Scheduling getScheduling() {
        return scheduling;
    }

    public List<String> getAdminEmails() {
        return adminEmails;
    }

    public void setAdminEmails(List<String> adminEmails) {
        this.adminEmails = adminEmails;
    }

    public String getPagesLog() {
        return pagesLog;
    }

    public void setPagesLog(String pagesLog) {
        this.pagesLog = pagesLog;
    }

    public List<String> getMonitoredSites() {
        return monitoredSites;
    }

    public void setMonitoredSites(List<String> monitoredSites) {
        this.monitoredSites = monitoredSites;
    }

    /**
     * Sign-in with Google: the apps send a Google ID token, checked against Google's public keys
     */
    public static class Google {
        /** OAuth client ids whose ID tokens are accepted (web, iOS, desktop: the {@code aud} claim); none = disabled */
        private List<String> clientIds = new ArrayList<>();

        /** Google's public keys (cached), configurable for local checks */
        private String jwkSetUri = "https://www.googleapis.com/oauth2/v3/certs";

        public List<String> getClientIds() {
            return clientIds;
        }

        public void setClientIds(List<String> clientIds) {
            this.clientIds = clientIds;
        }

        public String getJwkSetUri() {
            return jwkSetUri;
        }

        public void setJwkSetUri(String jwkSetUri) {
            this.jwkSetUri = jwkSetUri;
        }
    }

    /**
     * External APIs, overridden by the integration tests (local fake servers)
     */
    public static class Urls {

        /** PRIM marketplace: real time ({@code /stop-monitoring}, {@code /estimated-timetable}), traffic info ({@code /disruptions_bulk}) and Navitia ({@code /v2/navitia}) */
        private String prim = "https://prim.iledefrance-mobilites.fr/marketplace";

        /** Opendatasoft dataset of the IDFM GTFS: its metadata give the current version, its records the file */
        private String gtfsDataset = "https://data.iledefrance-mobilites.fr/api/explore/v2.1/catalog/datasets/offre-horaires-tc-gtfs-idfm";

        /** Opendatasoft export of the elevator status of the stations (open data "État des ascenseurs", static key) */
        private String elevators = "https://data.iledefrance-mobilites.fr/api/explore/v2.1/catalog/datasets/etat-des-ascenseurs/exports/json";

        /** IGN Géoplateforme geocoder (addresses, places, reverse geocoding; no key, 50 requests/s per IP) */
        private String geoplateforme = "https://data.geopf.fr/geocodage";

        /** Opendatasoft export of the IDFM stop zones (zones de correspondance = GTFS stop areas), for their town */
        private String stopZones = "https://data.iledefrance-mobilites.fr/api/explore/v2.1/catalog/datasets/zones-de-correspondance/exports/json";

        /** Vélib GBFS open data (no key) */
        private String velibGbfs = "https://velib-metropole-opendata.smovengo.cloud/opendata/Velib_Metropole";

        public String getPrim() {
            return prim;
        }

        public void setPrim(String prim) {
            this.prim = prim;
        }

        public String getGtfsDataset() {
            return gtfsDataset;
        }

        public void setGtfsDataset(String gtfsDataset) {
            this.gtfsDataset = gtfsDataset;
        }

        public String getElevators() {
            return elevators;
        }

        public void setElevators(String elevators) {
            this.elevators = elevators;
        }

        public String getGeoplateforme() {
            return geoplateforme;
        }

        public void setGeoplateforme(String geoplateforme) {
            this.geoplateforme = geoplateforme;
        }

        public String getStopZones() {
            return stopZones;
        }

        public void setStopZones(String stopZones) {
            this.stopZones = stopZones;
        }

        public String getVelibGbfs() {
            return velibGbfs;
        }

        public void setVelibGbfs(String velibGbfs) {
            this.velibGbfs = velibGbfs;
        }
    }

    /**
     * Background tasks (GTFS watch, cleanups); off in the integration tests, which trigger what they test
     */
    public static class Scheduling {

        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * GTFS import
     */
    public static class Gtfs {

        /** An import doesn't start below this much free disk: the new schema sits next to the current one until the swap */
        private int minFreeDiskGb = 6;

        public int getMinFreeDiskGb() {
            return minFreeDiskGb;
        }

        public void setMinFreeDiskGb(int minFreeDiskGb) {
            this.minFreeDiskGb = minFreeDiskGb;
        }
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
