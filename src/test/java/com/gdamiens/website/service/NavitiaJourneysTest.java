package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.v2.JourneyOption;
import com.gdamiens.website.controller.object.v2.JourneyPlan.PageCursor;
import com.gdamiens.website.controller.object.v2.JourneySection;
import com.gdamiens.website.controller.object.v2.JourneySection.Kind;
import com.gdamiens.website.controller.object.v2.LineSummary;
import com.gdamiens.website.model.TransportMode;
import com.gdamiens.website.support.Fixtures;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NavitiaJourneysTest {

    private static final LineSummary METRO_1 = new LineSummary("C01371", "1", "Métro 1", TransportMode.METRO, "FFCD00", "000000");

    private static final LineSummary RER_A = new LineSummary("C01742", "A", "RER A", TransportMode.RER, "EB2132", "FFFFFF");

    private final JsonNode response = Fixtures.json("navitia/journeys.json");

    private final List<JourneyOption> journeys = response.path("journeys").values().stream()
        .map(journey -> NavitiaJourneys.toJourney(journey, Map.of("C01371", METRO_1, "C01742", RER_A)))
        .toList();

    @Test
    void readsTheJourney() {
        JourneyOption best = journeys.getFirst();

        assertThat(best.type()).isEqualTo("best");
        assertThat(best.tags()).containsExactly("walking", "ecologic");
        assertThat(best.departure()).isEqualTo(Instant.parse("2026-10-10T08:00:00Z"));
        assertThat(best.arrival()).isEqualTo(Instant.parse("2026-10-10T08:15:00Z"));
        assertThat(best.duration()).isEqualTo(900);
        assertThat(best.walkingDuration()).isEqualTo(300);
        assertThat(best.walkingDistance()).isEqualTo(350);
        assertThat(best.co2()).isEqualTo(12.5);
        assertThat(best.fare()).isEqualTo(215);
    }

    @Test
    void dropsZeroLengthLinks() {
        assertThat(journeys.getFirst().sections()).extracting(JourneySection::kind).containsExactly(Kind.WALK, Kind.TRANSIT);
    }

    @Test
    void readsAWalk() {
        JourneySection walk = journeys.getFirst().sections().getFirst();

        assertThat(walk.from().name()).isEqualTo("10 Rue de Rivoli (Paris)");
        assertThat(walk.from().lat()).isEqualTo(48.86);
        assertThat(walk.from().stopAreaId()).isNull();
        assertThat(walk.to().stopAreaId()).isEqualTo("IDFM:70001");
        assertThat(walk.steps()).singleElement().satisfies(step -> assertThat(step.instruction()).startsWith("Marchez"));
        assertThat(walk.length()).isEqualTo(350);
        assertThat(walk.shape()).hasSize(2);
    }

    @Test
    void readsARideWithItsGtfsLineAndDelay() {
        JourneySection ride = journeys.getFirst().sections().getLast();

        assertThat(ride.line()).isEqualTo(METRO_1);
        assertThat(ride.headsign()).isEqualTo("Gare de Lyon");
        assertThat(ride.realtime()).isTrue();
        assertThat(ride.delay()).isEqualTo(120);
        assertThat(ride.boardingPositions()).containsExactly("front");
        assertThat(ride.stops()).extracting(stop -> stop.name()).containsExactly("Châtelet", "Gare de Lyon");
        assertThat(ride.from().stopAreaId()).isEqualTo("IDFM:70001");
        assertThat(ride.to().stopAreaId()).isEqualTo("IDFM:70002");
    }

    @Test
    void readsStopAreasAsJourneyPoints() {
        JourneySection rer = journeys.get(1).sections().getFirst();

        assertThat(rer.line()).isEqualTo(RER_A);
        assertThat(rer.realtime()).isFalse();
        assertThat(rer.delay()).isNull();
        assertThat(rer.from().stopAreaId()).isEqualTo("IDFM:70001");
        assertThat(rer.headsign()).isEqualTo("Boissy-Saint-Léger");
    }

    @Test
    void buildsTheLineOfAnUnknownRoute() {
        LineSummary bus = journeys.get(2).sections().getFirst().line();

        assertThat(bus.id()).isNull();
        assertThat(bus.name()).isEqualTo("999");
        assertThat(bus.mode()).isEqualTo(TransportMode.BUS);
        assertThat(journeys.get(2).fare()).isNull();
    }

    @Test
    void readsThePageCursors() {
        List<JsonNode> links = response.path("links").values().stream().toList();

        assertThat(NavitiaJourneys.toCursor(links.get(0).path("href").asString()))
            .isEqualTo(new PageCursor(Instant.parse("2026-10-10T07:59:00Z"), true));
        assertThat(NavitiaJourneys.toCursor(links.get(1).path("href").asString()))
            .isEqualTo(new PageCursor(Instant.parse("2026-10-10T08:01:00Z"), false));
        assertThat(NavitiaJourneys.toCursor(null)).isNull();
    }
}
