package com.gdamiens.website.utils;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ParisTimeTest {

    @Test
    void readsGtfsTimesOfAServiceDay() {
        assertThat(ParisTime.ofServiceDay(LocalDate.of(2026, 10, 10), 8 * 3600 + 30 * 60))
            .isEqualTo(Instant.parse("2026-10-10T06:30:00Z"));
        // Past midnight: still the service day before
        assertThat(ParisTime.ofServiceDay(LocalDate.of(2026, 10, 10), 25 * 3600 + 10 * 60))
            .isEqualTo(Instant.parse("2026-10-10T23:10:00Z"));
    }

    @Test
    void countsGtfsTimesFromNoonMinusTwelveHoursOnDstDays() {
        // 25 October 2026, clocks go back at 3:00 (a 25 h day): 08:00 is 08:00 CET
        assertThat(ParisTime.ofServiceDay(LocalDate.of(2026, 10, 25), 8 * 3600))
            .isEqualTo(Instant.parse("2026-10-25T07:00:00Z"));
        // 29 March 2026, clocks go forward at 2:00 (a 23 h day): 08:00 is 08:00 CEST
        assertThat(ParisTime.ofServiceDay(LocalDate.of(2026, 3, 29), 8 * 3600))
            .isEqualTo(Instant.parse("2026-03-29T06:00:00Z"));
    }

    @Test
    void readsAndWritesNavitiaTimes() {
        Instant instant = Instant.parse("2026-10-03T10:31:16Z");

        assertThat(ParisTime.formatNavitia(instant)).isEqualTo("20261003T123116");
        assertThat(ParisTime.parseNavitia("20261003T123116")).isEqualTo(instant);
        assertThat(ParisTime.parseNavitia("not a time")).isNull();
        assertThat(ParisTime.parseNavitia(null)).isNull();
    }

    @Test
    void measuresDistances() {
        // Châtelet to Gare de Lyon: about 2.4 km
        assertThat(Geo.meters(48.8584, 2.3470, 48.8443, 2.3730)).isBetween(2300.0, 2500.0);
        assertThat(Geo.meters(48.8584, 2.3470, 48.8584, 2.3470)).isZero();
    }

    @Test
    void readsGeoJsonLineStrings() {
        assertThat(GeoJson.lineStringCoordinates("{\"type\":\"LineString\",\"coordinates\":[[2.34,48.85],[2.37,48.84]]}"))
            .containsExactly(new double[]{2.34, 48.85}, new double[]{2.37, 48.84});
        assertThat(GeoJson.lineStringCoordinates(null)).isEmpty();
    }
}
