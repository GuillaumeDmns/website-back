package com.gdamiens.website.service;

import com.gdamiens.website.client.StopCall;
import com.gdamiens.website.controller.object.v2.StopAreaSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DepartureServiceTest {

    @ParameterizedTest
    @CsvSource({
        "Gare de Lyon, Gare de Lyon, true",
        "Paris Gare de Lyon, Gare de Lyon, true",
        "GARE DE LYON (Paris), Gare de Lyon, true",
        "Châtelet, Chatelet, true",
        "Gare de Lyonnais, Gare de Lyon, false",
        "Nation, Gare de Lyon, false",
    })
    void recognizesVehiclesEndingHere(String destination, String stop, boolean terminating) {
        assertThat(DepartureService.isTerminating(destination, new StopAreaSummary("IDFM:1", stop, 0, 0, null, List.of())))
            .isEqualTo(terminating);
    }

    @ParameterizedTest
    @ValueSource(strings = {"POPI", "TETE31", "ZEUS"})
    void recognizesMissionCodes(String value) {
        assertThat(DepartureService.isMission(value)).isTrue();
        assertThat(DepartureService.isTrainNumber(value)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"154023", "8601"})
    void recognizesTrainNumbers(String value) {
        assertThat(DepartureService.isTrainNumber(value)).isTrue();
        assertThat(DepartureService.isMission(value)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"popi", "POPIPOPI", "12", "", "Gare de Lyon"})
    void ignoresOtherNames(String value) {
        assertThat(DepartureService.isMission(value)).isFalse();
        assertThat(DepartureService.isTrainNumber(value)).isFalse();
    }

    private static final Instant SEVEN_PM = Instant.parse("2026-10-12T19:00:00Z");

    private static StopCall call(String line, String destination, String stop, int minutes, int seconds, String train) {
        Instant time = SEVEN_PM.plusSeconds(minutes * 60L + seconds);
        return new StopCall(line, destination, null, time, null, null, "onTime", null, null, false, null, train, stop);
    }

    @Test
    void leavesOutTheBusesSeenAgainAtANeighbourStop() {
        // Bus 22 at Gare Saint-Lazare, then at Pasquier - Anjou, both in the stop area (measured 12 October 2026)
        List<StopCall> calls = List.of(
            call("C01074", "Porte de Saint-Cloud", "Pasquier - Anjou", 28, 34, null),
            call("C01074", "Porte de Saint-Cloud", "Pasquier - Anjou", 39, 30, null),
            call("C01074", "Porte de Saint-Cloud", "Gare Saint-Lazare", 28, 0, null),
            call("C01074", "Porte de Saint-Cloud", "Gare Saint-Lazare", 39, 0, null),
            // Another line at the neighbour stop stays
            call("C01024", "Gare Saint-Lazare", "Pasquier - Anjou", 30, 0, null));

        assertThat(DepartureService.withoutNeighbourStopRepeats(calls))
            .extracting(StopCall::stopName, StopCall::lineId)
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple("Gare Saint-Lazare", "C01074"),
                org.assertj.core.groups.Tuple.tuple("Gare Saint-Lazare", "C01074"),
                org.assertj.core.groups.Tuple.tuple("Pasquier - Anjou", "C01024"));
    }

    @Test
    void stillTellsTheStopFurtherAlongForFrequentBuses() {
        // Every 4 min: a Pasquier passage is also 3.5 min before the next Saint-Lazare one
        List<StopCall> calls = List.of(
            call("C01074", "Porte de Saint-Cloud", "Gare Saint-Lazare", 28, 0, null),
            call("C01074", "Porte de Saint-Cloud", "Pasquier - Anjou", 28, 30, null),
            call("C01074", "Porte de Saint-Cloud", "Gare Saint-Lazare", 32, 0, null),
            call("C01074", "Porte de Saint-Cloud", "Pasquier - Anjou", 32, 30, null));

        assertThat(DepartureService.withoutNeighbourStopRepeats(calls)).extracting(StopCall::stopName).containsOnly("Gare Saint-Lazare");
    }

    @Test
    void keepsTheTrainsOfAStationsPlatformsAndOtherTrains() {
        // Platforms of a station share its name: never compared
        List<StopCall> platforms = List.of(
            call("C01742", "Marne-la-Vallée", "Châtelet-Les Halles", 2, 0, null),
            call("C01742", "Marne-la-Vallée", "Châtelet-Les Halles", 4, 0, null));
        assertThat(DepartureService.withoutNeighbourStopRepeats(platforms)).hasSize(2);

        // Different train numbers are different trains, even at stops of different names
        List<StopCall> trains = List.of(
            call("C01727", "Versailles", "Gare d'Austerlitz", 2, 0, "PAUL12"),
            call("C01727", "Versailles", "Austerlitz souterrain", 3, 0, "PAUL14"));
        assertThat(DepartureService.withoutNeighbourStopRepeats(trains)).hasSize(2);
    }
}
