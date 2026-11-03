package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.v2.Departure;
import com.gdamiens.website.controller.object.v2.LineBranch;
import com.gdamiens.website.controller.object.v2.LineDetail;
import com.gdamiens.website.controller.object.v2.LineDirection;
import com.gdamiens.website.controller.object.v2.StopRef;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

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

    @Test
    void findsAStopAreaServedAtTwoStopsInARow() {
        // Like the bus 341 around Péreire: two stops of the stop area, one after the other
        LineDetail line = new LineDetail(null, List.of(new LineDirection(0, List.of(new LineBranch("Charles de Gaulle - Étoile", 10, List.of(
            stop("IDFM:71453"), stop("IDFM:71453"), stop("IDFM:71470"), stop("IDFM:71347"), stop("IDFM:71347")), List.of())))));

        assertThat(RideService.servesTwiceInARow(line, "IDFM:71453")).isTrue();
        assertThat(RideService.servesTwiceInARow(line, "IDFM:71347")).isTrue();
        assertThat(RideService.servesTwiceInARow(line, "IDFM:71470")).isFalse();
        assertThat(RideService.servesTwiceInARow(null, "IDFM:71453")).isFalse();
    }

    @Test
    void keepsEachVehicleOnceAtTwoStopsInARow() {
        Instant now = Instant.parse("2026-11-03T12:00:00Z");
        // Each bus a minute later at the second stop; buses every 15 min
        List<Departure> buses = List.of(departure(now, null), departure(now.plusSeconds(60), null), departure(now.plusSeconds(900), null),
            departure(now.plusSeconds(960), null));

        assertThat(RideService.onceEach(buses)).extracting(Departure::time).containsExactly(now, now.plusSeconds(900));
        // Two numbered trains are two trains, however close
        List<Departure> trains = List.of(departure(now, "154101"), departure(now.plusSeconds(120), "154103"));
        assertThat(RideService.onceEach(trains)).hasSize(2);
        // Further than a stop away: another vehicle
        assertThat(RideService.onceEach(List.of(departure(now, null), departure(now.plus(Duration.ofMinutes(4)), null)))).hasSize(2);
    }

    private static StopRef stop(String id) {
        return new StopRef(id, "Stop " + id, 48.88, 2.29);
    }

    private static Departure departure(Instant time, String trainNumber) {
        return new Departure(time, null, true, null, null, null, null, trainNumber);
    }
}
