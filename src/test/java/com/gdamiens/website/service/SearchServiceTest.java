package com.gdamiens.website.service;

import com.gdamiens.website.client.GeoplateformeClient;
import com.gdamiens.website.client.NavitiaClient;
import com.gdamiens.website.controller.object.v2.LineSummary;
import com.gdamiens.website.controller.object.v2.PlaceResult;
import com.gdamiens.website.controller.object.v2.SearchResult;
import com.gdamiens.website.support.Fixtures;
import com.gdamiens.website.model.TransportMode;
import com.gdamiens.website.repository.NetworkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SearchServiceTest {

    private static final PlaceResult CHATELET = new PlaceResult(PlaceResult.Type.STOP_AREA, "IDFM:71264", "Châtelet (Paris 1er)", 48.8584, 2.347, List.of());

    private final StopSearch stopSearch = mock(StopSearch.class);

    private final GeoplateformeClient geoplateformeClient = mock(GeoplateformeClient.class);

    private final NavitiaClient navitiaClient = mock(NavitiaClient.class);

    private SearchService searchService;

    @BeforeEach
    void setUp() {
        NetworkService networkService = mock(NetworkService.class);
        Map<String, LineSummary> lines = Stream.of(
                new LineSummary("C01383", "13", null, TransportMode.METRO, null, null),
                new LineSummary("C01742", "A", null, TransportMode.RER, null, null),
                new LineSummary("C01743", "B", null, TransportMode.RER, null, null),
                new LineSummary("C01384", "7bis", null, TransportMode.METRO, null, null),
                new LineSummary("C02013", "13", null, TransportMode.BUS, null, null),
                new LineSummary("C01396", "M", null, TransportMode.BUS, null, null),
                new LineSummary("C01405", "N13", null, TransportMode.NOCTILIEN, null, null),
                new LineSummary("C01999", "T3a", null, TransportMode.TRAM, null, null))
            .collect(Collectors.toMap(LineSummary::id, Function.identity()));
        when(networkService.getLines()).thenReturn(lines);
        when(stopSearch.search(anyString(), any(), any(), anyInt())).thenReturn(List.of());
        searchService = new SearchService(networkService, mock(NetworkRepository.class), stopSearch, geoplateformeClient, navitiaClient);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "13         | C01383,C02013",
        "metro 13   | C01383",
        "m13        | C01383",
        "bus 13     | C02013",
        "rer b      | C01743",
        "b          | C01743",
        "7bis       | C01384",
        "m          | C01396",
        "noctilien 13 | ",
        "n13        | C01405",
        "t3a        | C01999",
        "tram t3a   | C01999",
        "chatelet   | ",
    })
    void findsLinesByNameAndMode(String query, String ids) {
        assertThat(searchService.searchLines(SearchService.normalize(query)))
            .extracting(LineSummary::id)
            .containsExactly(ids == null ? new String[0] : ids.split(","));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "Métro 13    | metro 13",
        "  RER   B   | rer b",
        "Châtelet-Les Halles | chatelet-les halles",
        "L'Haÿ-les-Roses | l'hay-les-roses",
        "gare/lyon!  | gare lyon",
    })
    void normalizesQueries(String query, String normalized) {
        assertThat(SearchService.normalize(query)).isEqualTo(normalized);
    }

    @Test
    void readsTheAddressesAndPlacesOfIleDeFrance() {
        List<PlaceResult> places = SearchService.toGeoPlaces(Fixtures.json("geoplateforme/search.json"));

        // Not Lyon nor the Oise (next to Île-de-France), not the station (the GTFS has it), the town once
        assertThat(places).extracting(PlaceResult::name)
            .containsExactly("8 Rue de Rivoli 75004 Paris", "Tour Eiffel (Paris)", "Orly", "Aéroport de Paris-Orly");
        assertThat(places.getFirst()).isEqualTo(new PlaceResult(PlaceResult.Type.ADDRESS, "address:75104_8249_00008", "8 Rue de Rivoli 75004 Paris",
            48.855372, 2.360909, null));
        assertThat(places.get(1).type()).isEqualTo(PlaceResult.Type.POI);
        assertThat(places.get(1).id()).isEqualTo("poi:SURFACTI0000000000142427");
    }

    @Test
    void searchesStopsAndAddressesWithoutNavitia() {
        // The stops get the exact position, the Géoplateforme the area (about 1 km)
        when(stopSearch.search("chatelet", 48.8512, 2.3456, 10)).thenReturn(List.of(CHATELET));
        when(geoplateformeClient.search("chatelet", 48.85, 2.35, 15)).thenReturn(Fixtures.json("geoplateforme/search.json"));

        SearchResult result = searchService.search("Châtelet", 10, 48.8512, 2.3456);

        assertThat(result.places()).extracting(PlaceResult::type)
            .containsExactly(PlaceResult.Type.STOP_AREA, PlaceResult.Type.ADDRESS, PlaceResult.Type.POI, PlaceResult.Type.ADDRESS, PlaceResult.Type.POI);
        verify(navitiaClient, never()).getPlaces(anyString(), any(), any());
    }

    @Test
    void asksNavitiaWhenTheGeoplateformeFails() {
        when(stopSearch.search(anyString(), any(), any(), anyInt())).thenReturn(List.of(CHATELET));
        when(geoplateformeClient.search(anyString(), any(), any(), anyInt())).thenThrow(new IllegalStateException("503"));
        when(navitiaClient.getPlaces(anyString(), any(), any())).thenReturn(Fixtures.json("navitia/places.json"));

        SearchResult result = searchService.search("rivoli", 10, null, null);

        // Its stop areas are not needed: the GTFS found some
        verify(navitiaClient).getPlaces("rivoli", List.of("address", "poi"), 10);
        assertThat(result.places()).extracting(PlaceResult::name).contains("Châtelet (Paris 1er)", "10 Rue de Rivoli (Paris)");
    }

    @Test
    void asksNavitiaWhenNothingElseFound() {
        when(geoplateformeClient.search(anyString(), any(), any(), anyInt())).thenReturn(Fixtures.json("geoplateforme/empty.json"));
        when(navitiaClient.getPlaces(anyString(), any(), any())).thenReturn(Fixtures.json("navitia/places.json"));

        searchService.search("chatlet", 10, null, null);

        verify(navitiaClient).getPlaces("chatlet", List.of("stop_area", "address", "poi"), 10);
    }

    @Test
    void putsTheAddressesFirstForAHouseNumber() {
        PlaceResult address = new PlaceResult(PlaceResult.Type.ADDRESS, "address:1", "8 Rue de Rivoli 75004 Paris", 48.85, 2.36, null);
        List<PlaceResult> stops = List.of(CHATELET, CHATELET, CHATELET);

        assertThat(SearchService.merge("8 rue", stops, List.of(address), 3)).containsExactly(address, CHATELET, CHATELET);
        assertThat(SearchService.merge("rivoli", stops, List.of(address, address), 4)).containsExactly(CHATELET, CHATELET, address, address);
        assertThat(SearchService.merge("rivoli", stops, List.of(), 4)).containsExactly(CHATELET, CHATELET, CHATELET);
    }

    @Test
    void namesAPositionAfterItsAddress() {
        when(geoplateformeClient.reverse(48.8584, 2.347)).thenReturn(Fixtures.json("geoplateforme/reverse.json"));

        assertThat(searchService.reverse(48.85841, 2.34702)).contains(
            new PlaceResult(PlaceResult.Type.ADDRESS, "address:75101_4974_00002", "2 Rue Jean Lantier 75001 Paris", 48.8584, 2.347, null));
        // Too far: no address here
        when(geoplateformeClient.reverse(anyDouble(), eq(2.5))).thenReturn(Fixtures.json("geoplateforme/far.json"));
        assertThat(searchService.reverse(48.9, 2.5)).isEqualTo(Optional.empty());
    }
}
