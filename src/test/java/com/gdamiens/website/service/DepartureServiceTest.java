package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.v2.StopAreaSummary;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

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
}
