package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.v2.Disruption;
import com.gdamiens.website.controller.object.v2.Disruption.Category;
import com.gdamiens.website.controller.object.v2.Disruption.Severity;
import com.gdamiens.website.support.Fixtures;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TrafficServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-10T10:00:00Z");

    private final TrafficService.Snapshot snapshot = TrafficService.parse(Fixtures.json("navitia/traffic_reports.json"), NOW);

    @Test
    void readsActiveDisruptionsOfALine() {
        Disruption stopped = snapshot.forLine("C01371").stream()
            .filter(disruption -> disruption.id().equals("metro-1-stopped")).findFirst().orElseThrow();

        assertThat(stopped.severity()).isEqualTo(Severity.BLOCKING);
        assertThat(stopped.category()).isEqualTo(Category.TRAFFIC);
        assertThat(stopped.active()).isTrue();
        assertThat(stopped.title()).isEqualTo("Métro 1 : trafic interrompu");
        assertThat(stopped.message()).isEqualTo("Trafic interrompu entre Châtelet et Nation.\n\nReprise estimée : 18h.");
        assertThat(stopped.lineIds()).containsExactly("C01371");
        assertThat(stopped.updatedAt()).isEqualTo(Instant.parse("2026-10-10T06:00:00Z"));
    }

    @Test
    void leavesOutPastDisruptionsAndCanceledTrips() {
        assertThat(snapshot.forLine("C01371")).extracting(Disruption::id).doesNotContain("metro-1-past");
        assertThat(snapshot.forLine("C02000")).extracting(Disruption::id).containsExactly("bus-38-delays");
        assertThat(snapshot.forLine("C02000").getFirst().severity()).isEqualTo(Severity.DISRUPTED);
    }

    @Test
    void keepsUpcomingWorksOfTheWeekWithTheirNextPeriod() {
        assertThat(snapshot.forLine("C01742")).singleElement().satisfies(works -> {
            assertThat(works.id()).isEqualTo("rer-a-works");
            assertThat(works.active()).isFalse();
            assertThat(works.category()).isEqualTo(Category.WORKS);
            assertThat(works.severity()).isEqualTo(Severity.DISRUPTED);
            // The earliest period still to come, although listed second
            assertThat(works.start()).isEqualTo(Instant.parse("2026-10-12T20:00:00Z"));
            assertThat(works.end()).isEqualTo(Instant.parse("2026-10-13T03:00:00Z"));
            assertThat(works.message()).isEqualTo("Pas de train entre Vincennes et Fontenay.\nBus de remplacement.");
        });
    }

    @Test
    void readsElevatorOutagesOfAStopArea() {
        assertThat(snapshot.forStopArea("IDFM:70001")).singleElement().satisfies(elevator -> {
            assertThat(elevator.category()).isEqualTo(Category.ELEVATOR);
            assertThat(elevator.severity()).isEqualTo(Severity.INFO);
            assertThat(elevator.lineIds()).isEmpty();
        });
    }

    @Test
    void countsTheKeptDisruptions() {
        // Two metro, one elevator, one RER works, one bus delay
        assertThat(snapshot.disruptionCount()).isEqualTo(5);
    }

    @Test
    void turnsHtmlMessagesIntoPlainText() {
        assertThat(TrafficService.toPlainText("<p>Ligne&nbsp;1&#160;: <b>interrompue</b></p><ul><li>Bus &amp; tram</li><li>RER&#x20;A</li></ul>"))
            .isEqualTo("Ligne 1 : interrompue\n\n• Bus & tram\n• RER A");
        assertThat(TrafficService.toPlainText("L&rsquo;arrêt &laquo;Nation&raquo; &unknown;"))
            .isEqualTo("L’arrêt «Nation» &unknown;");
        assertThat(TrafficService.toPlainText("  ")).isNull();
        assertThat(TrafficService.toPlainText(null)).isNull();
    }
}
