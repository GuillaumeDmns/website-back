package com.gdamiens.website.service;

import com.gdamiens.website.client.DisruptionsClient;
import com.gdamiens.website.client.ElevatorClient;
import com.gdamiens.website.controller.object.v2.Disruption;
import com.gdamiens.website.controller.object.v2.Disruption.Category;
import com.gdamiens.website.controller.object.v2.Disruption.Section;
import com.gdamiens.website.controller.object.v2.Disruption.Severity;
import com.gdamiens.website.controller.object.v2.StopAreaDetail;
import com.gdamiens.website.repository.NetworkRepository;
import com.gdamiens.website.support.Fixtures;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrafficServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-10T10:00:00Z");

    /** The GTFS of the tests: bus quay of Châtelet, stop area names */
    private static final TrafficService.StopLookup STOPS = new TrafficService.StopLookup() {
        @Override
        public Map<String, String> stopAreasOfQuays(Collection<String> quayIds) {
            return Map.of("IDFM:1003", "IDFM:70001");
        }

        @Override
        public Map<String, String> names(Collection<String> stopAreaIds) {
            return Map.of("IDFM:70001", "Châtelet");
        }
    };

    private final TrafficService.Snapshot snapshot = TrafficService.parse(Fixtures.json("prim/disruptions_bulk.json"), STOPS, NOW);

    @Test
    void readsActiveDisruptionsOfALine() {
        Disruption stopped = snapshot.forLine("C01371").stream()
            .filter(disruption -> disruption.id().equals("metro-1-stopped")).findFirst().orElseThrow();

        assertThat(stopped.severity()).isEqualTo(Severity.BLOCKING);
        assertThat(stopped.category()).isEqualTo(Category.TRAFFIC);
        assertThat(stopped.cause()).isEqualTo("PERTURBATION");
        assertThat(stopped.active()).isTrue();
        assertThat(stopped.title()).isEqualTo("Métro 1 : trafic interrompu");
        assertThat(stopped.message()).isEqualTo("Trafic interrompu entre Châtelet et Nation.\n\nReprise estimée : 18h.");
        assertThat(stopped.lineIds()).containsExactly("C01371");
        // Paris time
        assertThat(stopped.updatedAt()).isEqualTo(Instant.parse("2026-10-10T06:00:00Z"));
    }

    @Test
    void namesTheImpactedSectionsLikeTheGtfs() {
        Disruption stopped = snapshot.forLine("C01371").stream()
            .filter(disruption -> disruption.id().equals("metro-1-stopped")).findFirst().orElseThrow();

        // Nation is not in the lookup: IDFM's name stays. Listed both ways: once.
        assertThat(stopped.sections()).containsExactly(new Section("C01371", "IDFM:70001", "Châtelet", "IDFM:70003", "Nation (Paris)"));
    }

    @Test
    void leavesOutPastDisruptionsAndTheFarFuture() {
        assertThat(snapshot.forLine("C01371")).extracting(Disruption::id).containsExactly("metro-1-stopped", "metro-1-stopped-again");
        assertThat(snapshot.forLine("C01742")).extracting(Disruption::id).containsExactly("rer-a-works");
    }

    @Test
    void keepsUpcomingWorksOfTheWeekWithTheirNextPeriod() {
        assertThat(snapshot.forLine("C01742")).singleElement().satisfies(works -> {
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
    void anImpactOnTheNetworkIsOneOnTheLine() {
        assertThat(snapshot.forLine("C02000")).extracting(Disruption::id).containsExactlyInAnyOrder("bus-38-delays", "bus-network-info");
        assertThat(snapshot.forLine("C02000")).extracting(Disruption::severity).containsExactlyInAnyOrder(Severity.DISRUPTED, Severity.INFO);
    }

    @Test
    void anImpactOnAStopPointBelongsToItsStopArea() {
        assertThat(snapshot.forStopArea("IDFM:70001")).singleElement().satisfies(closed -> {
            assertThat(closed.id()).isEqualTo("chatelet-bus-stop-closed");
            assertThat(closed.lineIds()).isEmpty();
        });
        assertThat(snapshot.forLine("C02000")).extracting(Disruption::id).doesNotContain("chatelet-bus-stop-closed");
    }

    @Test
    void countsTheKeptDisruptions() {
        assertThat(snapshot.disruptionCount()).isEqualTo(6);
    }

    @Test
    void readsTheElevatorsOutOfOrder() {
        Map<String, List<Disruption>> elevators = TrafficService.parseElevators(Fixtures.json("opendata/etat-des-ascenseurs.json"));

        // Available and unknown ones left out
        assertThat(elevators).containsOnlyKeys("IDFM:70001", "IDFM:70003");
        assertThat(elevators.get("IDFM:70001")).singleElement().satisfies(elevator -> {
            assertThat(elevator.id()).isEqualTo("RATP:LiftEquipment:1:LOC");
            assertThat(elevator.category()).isEqualTo(Category.ELEVATOR);
            assertThat(elevator.severity()).isEqualTo(Severity.INFO);
            assertThat(elevator.active()).isTrue();
            assertThat(elevator.title()).isEqualTo("Ascenseur hors service : Ligne M1 direction Nation");
            assertThat(elevator.message()).isEqualTo("Niveau 0 : sortie 2, rue de Rivoli");
            assertThat(elevator.cause()).isEqualTo("liftFailure");
            assertThat(elevator.start()).isEqualTo(Instant.parse("2026-10-09T04:11:00Z"));
            assertThat(elevator.lineIds()).isEmpty();
        });
        assertThat(elevators.get("IDFM:70003")).singleElement()
            .satisfies(elevator -> assertThat(elevator.title()).isEqualTo("Ascenseur hors service"));
    }

    @Test
    void withoutElevatorDataTheStopsKeepTheirTrafficAndTryAgainLater() {
        DisruptionsClient disruptionsClient = mock(DisruptionsClient.class);
        when(disruptionsClient.getDisruptions()).thenReturn(Fixtures.json("prim/disruptions_bulk.json"));
        ElevatorClient elevatorClient = mock(ElevatorClient.class);
        when(elevatorClient.getElevators()).thenThrow(new IllegalStateException("Opendatasoft is down"));
        NetworkRepository networkRepository = mock(NetworkRepository.class);
        when(networkRepository.findStopAreasOfQuays(any())).thenReturn(Map.of("IDFM:1003", "IDFM:70001"));
        TrafficService service = new TrafficService(disruptionsClient, elevatorClient, mock(NetworkService.class), networkRepository);
        StopAreaDetail chatelet = new StopAreaDetail("IDFM:70001", "Châtelet", 48.8584, 2.3470, (short) 0, List.of(), List.of(), List.of());

        assertThat(service.getStopDisruptions(chatelet)).extracting(Disruption::id).containsExactly("chatelet-bus-stop-closed");
        assertThat(service.getStopDisruptions(chatelet)).hasSize(1);

        // Not asked again at each request: in 5 min
        verify(elevatorClient, times(1)).getElevators();
        verify(disruptionsClient, times(1)).getDisruptions();
        assertThat(service.elevatorsSnapshotTime()).isPresent();
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
