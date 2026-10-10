package com.gdamiens.website.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class TransportModeTest {

    @ParameterizedTest
    @CsvSource(nullValues = "null", value = {
        "0, RATP, T3a, TRAM",
        "6, RATP, Câble, TRAM",
        "7, RATP, Funiculaire, TRAM",
        "1, RATP, 14, METRO",
        "2, RER, A, RER",
        "2, TER, TER, TER",
        "2, Transilien, L, TRANSILIEN",
        "2, null, P, TRANSILIEN",
        "3, RATP, 38, BUS",
        "3, Noctilien, N11, NOCTILIEN",
        "3, RATP, N, BUS",
        "null, null, 38, BUS",
    })
    void mapsGtfsRoutes(Short routeType, String agency, String shortName, TransportMode expected) {
        assertThat(TransportMode.fromGtfs(routeType, agency, shortName)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"C01371, IDFM:C01371"})
    void convertsLineIds(String lineId, String routeId) {
        assertThat(IDFMRoute.toRouteId(lineId)).isEqualTo(routeId);
        assertThat(IDFMRoute.toLineId(routeId)).isEqualTo(lineId);
    }
}
