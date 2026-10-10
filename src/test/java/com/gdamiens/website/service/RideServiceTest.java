package com.gdamiens.website.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class RideServiceTest {

    @ParameterizedTest
    @CsvSource(nullValues = "null", value = {
        "Gare de Lyon, Paris Gare de Lyon, true",
        "Marne-la-Vallée Chessy, Marne la Vallee - Chessy, true",
        "Gare du Nord, Nord, true",
        "Saint-Germain-en-Laye, Boissy-Saint-Léger, false",
        "Gare de Lyon, Gare du Nord, false",
        "null, Nation, false",
        "Gare, Paris, false",
    })
    void comparesDestinationNames(String a, String b, boolean same) {
        assertThat(RideService.sameName(a, b)).isEqualTo(same);
    }
}
