package com.gdamiens.website.service;

import com.gdamiens.website.client.NavitiaClient;
import com.gdamiens.website.controller.object.v2.LineSummary;
import com.gdamiens.website.model.TransportMode;
import com.gdamiens.website.repository.NetworkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SearchServiceTest {

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
        searchService = new SearchService(networkService, mock(NetworkRepository.class), mock(NavitiaClient.class));
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
}
