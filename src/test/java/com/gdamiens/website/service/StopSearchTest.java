package com.gdamiens.website.service;

import com.gdamiens.website.client.StopZoneClient;
import com.gdamiens.website.controller.object.v2.LineSummary;
import com.gdamiens.website.controller.object.v2.PlaceResult;
import com.gdamiens.website.model.TransportMode;
import com.gdamiens.website.repository.NetworkRepository;
import com.gdamiens.website.repository.NetworkRepository.ServedStopAreaRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StopSearchTest {

    private static final LineSummary METRO_1 = new LineSummary("C01371", "1", null, TransportMode.METRO, null, null);

    private static final LineSummary BUS_38 = new LineSummary("C02000", "38", null, TransportMode.BUS, null, null);

    private final NetworkRepository networkRepository = mock(NetworkRepository.class);

    private final StopZoneClient stopZoneClient = mock(StopZoneClient.class);

    private StopSearch stopSearch;

    @BeforeEach
    void setUp() {
        when(networkRepository.findServedStopAreas()).thenReturn(List.of(
            new ServedStopAreaRow("IDFM:71264", "Châtelet", 48.8584, 2.3470, List.of("metro")),
            new ServedStopAreaRow("IDFM:60001", "Châtelet", 48.8800, 2.5900, List.of("bus")),
            new ServedStopAreaRow("IDFM:71410", "Gare de Lyon", 48.8443, 2.3730, List.of("metro")),
            new ServedStopAreaRow("IDFM:71370", "Saint-Lazare", 48.8763, 2.3253, List.of("metro")),
            new ServedStopAreaRow("IDFM:60002", "Mairie", 48.8620, 2.4400, List.of("bus")),
            new ServedStopAreaRow("IDFM:60003", "Mairie", 48.7470, 2.3550, List.of("bus")),
            new ServedStopAreaRow("IDFM:71800", "Mairie de Montreuil", 48.8620, 2.4420, List.of("metro")),
            new ServedStopAreaRow("IDFM:70004", "Vincennes", 48.8474, 2.4334, List.of("metro"))));
        NetworkService networkService = mock(NetworkService.class);
        when(networkService.toLineSummaries(anyCollection())).thenAnswer(call ->
            ((java.util.Collection<?>) call.getArgument(0)).contains("metro") ? List.of(METRO_1) : List.of(BUS_38));
        when(stopZoneClient.getTowns()).thenReturn(Map.of(
            "IDFM:71264", "Paris 1er", "IDFM:60001", "Chelles", "IDFM:71410", "Paris 12e", "IDFM:71370", "Paris 8e",
            "IDFM:60002", "Montreuil", "IDFM:60003", "Rungis", "IDFM:71800", "Montreuil", "IDFM:70004", "Vincennes"));
        stopSearch = new StopSearch(networkRepository, networkService, stopZoneClient);
    }

    private List<String> names(String query) {
        return stopSearch.search(query, null, null, 10).stream().map(PlaceResult::name).toList();
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        // Accents, case, unfinished words, typos: the metro station before the bus stop of the same name
        "chatelet        | Châtelet (Paris 1er),Châtelet (Chelles)",
        "CHÂT            | Châtelet (Paris 1er),Châtelet (Chelles)",
        "chatlet         | Châtelet (Paris 1er),Châtelet (Chelles)",
        // Abbreviations, hyphens, small words left out
        "st lazare       | Saint-Lazare (Paris 8e)",
        "saint lazar     | Saint-Lazare (Paris 8e)",
        "gare lyon       | Gare de Lyon (Paris 12e)",
        "gare de lyo     | Gare de Lyon (Paris 12e)",
        // The town tells the stops of the same name apart
        "chatelet chelles | Châtelet (Chelles)",
        "mairie montreuil | Mairie de Montreuil,Mairie (Montreuil)",
        // The town only when the name doesn't already say it
        "vincennes       | Vincennes",
        "nowhere         | ",
    })
    void findsStopAreas(String query, String expected) {
        assertThat(names(query)).containsExactly(expected == null ? new String[0] : expected.split(","));
    }

    @Test
    void closerStopsComeFirst() {
        // Near Rungis
        assertThat(stopSearch.search("mairie", 48.7470, 2.3550, 10)).extracting(PlaceResult::name)
            .containsExactly("Mairie (Rungis)", "Mairie (Montreuil)", "Mairie de Montreuil");
    }

    @Test
    void givesTheLinesAndPositionOfEachStopArea() {
        assertThat(stopSearch.search("gare de lyon", null, null, 10)).singleElement().satisfies(place -> {
            assertThat(place.type()).isEqualTo(PlaceResult.Type.STOP_AREA);
            assertThat(place.id()).isEqualTo("IDFM:71410");
            assertThat(place.lat()).isEqualTo(48.8443);
            assertThat(place.lines()).containsExactly(METRO_1);
        });
    }

    @Test
    void withoutTheTownsTheNamesStayPlain() {
        when(stopZoneClient.getTowns()).thenThrow(new IllegalStateException("Opendatasoft is down"));

        assertThat(names("gare de lyon")).containsExactly("Gare de Lyon");
        names("chatelet");

        // Not tried again at each search
        verify(stopZoneClient, times(1)).getTowns();
    }

    @Test
    void isBuiltAgainAfterAGtfsImport() {
        names("chatelet");
        names("mairie");
        stopSearch.onGtfsImported();
        names("chatelet");

        verify(networkRepository, times(2)).findServedStopAreas();
    }

    @Test
    void anAdministratorBuildsItAgain() {
        names("chatelet");
        when(networkRepository.findServedStopAreas()).thenReturn(List.of(
            new ServedStopAreaRow("IDFM:71264", "Châtelet", 48.8584, 2.3470, List.of("metro"))));

        assertThat(stopSearch.rebuild()).isEqualTo(1);
        assertThat(names("chatelet")).containsExactly("Châtelet (Paris 1er)");
        verify(networkRepository, times(2)).findServedStopAreas();
    }

    @Test
    void countsTypos() {
        assertThat(StopSearch.distance("chatlet", "chatelet", 1)).isEqualTo(1);
        assertThat(StopSearch.distance("lazare", "lazare", 1)).isZero();
        assertThat(StopSearch.distance("montreuil", "montrouge", 2)).isEqualTo(3);
        assertThat(StopSearch.wordMatch("mont", "montreuil")).isEqualTo(2);
        assertThat(StopSearch.wordMatch("mnotreuil", "montreuil")).isEqualTo(1);
        assertThat(StopSearch.wordMatch("abc", "abd")).isZero();
    }
}
