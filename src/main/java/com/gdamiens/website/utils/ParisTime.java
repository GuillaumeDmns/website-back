package com.gdamiens.website.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Times of the Île-de-France network: GTFS service days and Navitia local times are Europe/Paris
 */
public final class ParisTime {

    public static final ZoneId ZONE = ZoneId.of("Europe/Paris");

    /** Navitia local time, e.g. {@code 20261003T123116} */
    private static final DateTimeFormatter NAVITIA = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    private ParisTime() {
    }

    /**
     * GTFS time of a service day: seconds counted from noon minus 12 h (may go past 24:00), which differs from
     * midnight on DST change days
     */
    public static Instant ofServiceDay(LocalDate serviceDate, int seconds) {
        return ZonedDateTime.of(serviceDate, LocalTime.NOON, ZONE).minusHours(12).plusSeconds(seconds).toInstant();
    }

    /** @return null when absent or not understood */
    public static Instant parseNavitia(String value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, NAVITIA).atZone(ZONE).toInstant();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static String formatNavitia(Instant instant) {
        return NAVITIA.format(instant.atZone(ZONE));
    }
}
