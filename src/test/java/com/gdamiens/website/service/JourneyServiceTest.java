package com.gdamiens.website.service;

import com.gdamiens.website.exceptions.CustomException;
import com.gdamiens.website.model.TransportMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JourneyServiceTest {

    @Test
    void turnsPlacesIntoNavitiaOnes() {
        assertThat(JourneyService.toNavitiaPlace("48.8443,2.3730")).isEqualTo("2.3730;48.8443");
        assertThat(JourneyService.toNavitiaPlace(" -48.5,-2 ")).isEqualTo("-2;-48.5");
        assertThat(JourneyService.toNavitiaPlace("IDFM:71264")).isEqualTo("stop_area:IDFM:71264");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "Châtelet", "IDFM:monomodalStopPlace:1", "48.8;2.3", "stop_area:IDFM:71264"})
    void refusesOtherPlaces(String place) {
        assertThatThrownBy(() -> JourneyService.toNavitiaPlace(place)).isInstanceOf(CustomException.class);
    }

    @Test
    void forbidsThePhysicalModesOfTheModesLeftOut() {
        assertThat(JourneyService.forbiddenModes(List.of())).isEmpty();
        assertThat(JourneyService.forbiddenModes(null)).isEmpty();

        assertThat(JourneyService.forbiddenModes(List.of(TransportMode.METRO, TransportMode.RER)))
            .contains("physical_mode:Bus", "physical_mode:Tramway", "physical_mode:LocalTrain", "physical_mode:Train")
            .doesNotContain("physical_mode:Metro", "physical_mode:RapidTransit");
    }

    @Test
    void closeSearchesShareTheirAnswer() {
        JourneyService.JourneyQuery here = new JourneyService.JourneyQuery("48.85843,2.34702", "IDFM:70002",
            java.time.Instant.parse("2026-10-10T08:30:42Z"), false, List.of(TransportMode.RER, TransportMode.METRO), false, null, null, false);
        JourneyService.JourneyQuery nextDoor = new JourneyService.JourneyQuery("48.85851,2.34711", " IDFM:70002 ",
            java.time.Instant.parse("2026-10-10T08:30:05Z"), false, List.of(TransportMode.METRO, TransportMode.RER), false, null, null, false);

        assertThat(JourneyService.withSharedPlaces(here)).isEqualTo(JourneyService.withSharedPlaces(nextDoor));
        assertThat(JourneyService.withSharedPlaces(here).from()).isEqualTo("48.8585,2.3470");
        assertThat(JourneyService.withSharedPlaces(here).datetime()).isEqualTo(java.time.Instant.parse("2026-10-10T08:30:00Z"));

        JourneyService.JourneyQuery farther = new JourneyService.JourneyQuery("48.8600,2.3470", "IDFM:70002",
            java.time.Instant.parse("2026-10-10T08:30:00Z"), false, List.of(), false, null, null, false);
        assertThat(JourneyService.withSharedPlaces(farther)).isNotEqualTo(JourneyService.withSharedPlaces(here));
    }

    @Test
    void keepsBusesWhenOnlyNightBusesAreAllowed() {
        // Noctilien and day buses share the Bus physical mode
        assertThat(JourneyService.forbiddenModes(List.of(TransportMode.NOCTILIEN))).doesNotContain("physical_mode:Bus");
    }
}
