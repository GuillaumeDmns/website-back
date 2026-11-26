package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.v2.LineSummary;
import com.gdamiens.website.model.TransportMode;
import com.gdamiens.website.repository.NetworkRepository.RouteRow;
import com.gdamiens.website.repository.NetworkRepository.TripPatternRow;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class NetworkServiceTest {

    @Test
    void tellsReplacementBusesWhoeverRunsThem() {
        // As in the GTFS of November 2026
        List<LineSummary> lines = NetworkService.toLineSummaries(List.of(
            new RouteRow("IDFM:C01740", "L", "L", (short) 2, "D282BE", "FFFFFF", "Transilien"),
            new RouteRow("IDFM:C01847", "L", "Remplacement Transilien L", (short) 3, "D282BE", "FFFFFF", "Transilien"),
            // Handed to another operator: the line and its replacement buses with it
            new RouteRow("IDFM:C01843", "T4", "T4", (short) 0, "DC9600", "000000", "STRETTO"),
            new RouteRow("IDFM:C01683", "T4", "T4", (short) 3, "dc9600", "000000", "STRETTO"),
            // Named so, its badge another color than the train's
            new RouteRow("IDFM:C01844", "K", "Remplacement Transilien K", (short) 3, "9B9842", "FFFFFF", "Transilien"),
            // An ordinary bus of the same name, in its own color
            new RouteRow("IDFM:C00001", "L", "Gare - Centre", (short) 3, "0064B0", "FFFFFF", "Keolis")));

        assertThat(lines).extracting(LineSummary::id, LineSummary::replacement, LineSummary::mode).containsExactly(
            org.assertj.core.groups.Tuple.tuple("C01740", false, TransportMode.TRANSILIEN),
            org.assertj.core.groups.Tuple.tuple("C01847", true, TransportMode.BUS),
            org.assertj.core.groups.Tuple.tuple("C01843", false, TransportMode.TRAM),
            org.assertj.core.groups.Tuple.tuple("C01683", true, TransportMode.BUS),
            org.assertj.core.groups.Tuple.tuple("C01844", true, TransportMode.BUS),
            org.assertj.core.groups.Tuple.tuple("C00001", false, TransportMode.BUS));
    }

    @Test
    void anotherOperatorKeepsATrainATrain() {
        assertThat(TransportMode.fromGtfs((short) 2, "Keolis", "L")).isEqualTo(TransportMode.TRANSILIEN);
    }

    @Test
    void ordersLinesByModeThenNaturalName() {
        List<LineSummary> lines = Stream.of(
                line("10", TransportMode.METRO), line("B", TransportMode.RER), line("2", TransportMode.METRO),
                line("38", TransportMode.BUS), line("T3b", TransportMode.TRAM), line("T3a", TransportMode.TRAM),
                line("N12", TransportMode.NOCTILIEN), line("N1", TransportMode.NOCTILIEN), line("A", TransportMode.RER),
                line("7bis", TransportMode.METRO), line("7", TransportMode.METRO), line("L", TransportMode.TRANSILIEN),
                line("T4", TransportMode.TRAM), line(null, TransportMode.BUS))
            .sorted(NetworkService.LINE_ORDER)
            .toList();

        assertThat(lines).extracting(LineSummary::name).containsExactly(
            "2", "7", "7bis", "10", "A", "B", "L", "T3a", "T3b", "T4", "38", null, "N1", "N12");
    }

    @Test
    void mergesShortTurnsAndExpressServicesIntoTheirBranch() {
        TripPatternRow all = pattern(0, 10, "A", "B", "C", "D", "E");
        TripPatternRow shortTurn = pattern(0, 4, "A", "B", "C");
        TripPatternRow express = pattern(0, 3, "A", "C", "E");

        List<TripPatternRow> branches = NetworkService.mergeIncludedPatterns(List.of(shortTurn, express, all));

        assertThat(branches).hasSize(1);
        assertThat(branches.getFirst().stopAreaIds()).containsExactly("A", "B", "C", "D", "E");
        assertThat(branches.getFirst().tripCount()).isEqualTo(17);
    }

    @Test
    void addsStopsOnlyServedByAVariant() {
        TripPatternRow main = pattern(0, 10, "A", "B", "D");
        TripPatternRow detour = pattern(0, 2, "A", "C", "D");

        List<TripPatternRow> branches = NetworkService.mergeIncludedPatterns(List.of(main, detour));

        assertThat(branches).hasSize(1);
        assertThat(branches.getFirst().stopAreaIds()).containsExactly("A", "B", "C", "D");
    }

    @Test
    void keepsForksAsBranchesMostFrequentFirst() {
        TripPatternRow toFontenay = pattern(0, 4, "A", "B", "C", "F");
        TripPatternRow toJoinville = pattern(0, 8, "A", "B", "C", "J");

        List<TripPatternRow> branches = NetworkService.mergeIncludedPatterns(List.of(toFontenay, toJoinville));

        assertThat(branches).extracting(branch -> branch.stopAreaIds().getLast()).containsExactly("J", "F");
    }

    @Test
    void keepsAPatternInAnotherOrderApart() {
        TripPatternRow forth = pattern(0, 4, "A", "B", "C");
        TripPatternRow back = pattern(0, 4, "C", "B", "A");

        assertThat(NetworkService.mergeIncludedPatterns(List.of(forth, back))).hasSize(2);
    }

    @Test
    void orientsPatternsAgainstTheMainOneWhenDirectionIdsAreWrong() {
        // Both ways of the RER A are in direction 0 in the GTFS
        TripPatternRow east = pattern(0, 20, "A", "B", "C", "D");
        TripPatternRow west = pattern(0, 18, "D", "C", "B", "A");
        TripPatternRow westShort = pattern(1, 5, "C", "B");
        TripPatternRow elsewhere = pattern(1, 2, "X", "Y");

        List<TripPatternRow> oriented = NetworkService.orientPatterns(List.of(east, west, westShort, elsewhere));

        assertThat(oriented).extracting(TripPatternRow::directionId).containsExactly(0, 1, 1, 1);
    }

    @Test
    void leavesPatternsAloneWithoutDirectionZero() {
        List<TripPatternRow> patterns = List.of(pattern(1, 3, "A", "B"));

        assertThat(NetworkService.orientPatterns(patterns)).isEqualTo(patterns);
    }

    private static TripPatternRow pattern(int direction, int trips, String... stops) {
        return new TripPatternRow(direction, List.of(stops), trips, "shape-" + String.join("", stops), stops[stops.length - 1]);
    }

    private static LineSummary line(String name, TransportMode mode) {
        return new LineSummary("id-" + name, name, null, mode, null, null);
    }
}
