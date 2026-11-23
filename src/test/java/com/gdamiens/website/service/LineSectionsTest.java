package com.gdamiens.website.service;

import com.gdamiens.website.service.LineSections.Branch;
import com.gdamiens.website.service.LineSections.Pattern;
import com.gdamiens.website.service.LineSections.Result;
import com.gdamiens.website.service.LineSections.Rules;
import com.gdamiens.website.service.LineSections.Section;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class LineSectionsTest {

    private static final double LAT = 48.85;
    private static final double LON = 2.35;
    private static final double METERS_PER_DEGREE = 6_371_000 * Math.PI / 180;

    /** Stops on an east-west line, 1 km apart */
    private static final Map<String, double[]> STOPS = Map.of(
        "A", at(0, 0), "B", at(1000, 0), "C", at(2000, 0), "D", at(1000, -400));

    @Test
    void bothWaysOnTheSameTracksShareOneSection() {
        // The other way's track 4 m to the north
        Result result = LineSections.build(List.of(
                pattern(0, "S0", List.of("A", "B", "C"), xy(0, 0), xy(2000, 0)),
                pattern(1, "S1", List.of("C", "B", "A"), xy(2000, 4), xy(0, 4))),
            List.of(new Branch(0, List.of("A", "B", "C")), new Branch(1, List.of("C", "B", "A"))),
            STOPS, Rules.TRACKS);

        assertThat(result.sections()).hasSize(2);
        assertThat(result.sections()).allSatisfy(section -> assertThat(section.directionIds()).containsExactly(0, 1));
        assertThat(result.branchSections()).containsExactly(List.of(0, 1), List.of(1, 0));
        // Each branch runs along the same points, its own way
        assertThat(result.branchShapes().get(1)).usingElementComparator(LineSectionsTest::compare)
            .containsExactlyElementsOf(result.branchShapes().get(0).reversed());
        assertThat(length(result.branchShapes().getFirst())).isCloseTo(2000, within(10.0));
    }

    @Test
    void aBusOnOneWayStreetsHasASectionEachWay() {
        // Back by a street 150 m to the north
        Result result = LineSections.build(List.of(
                pattern(0, "S0", List.of("A", "B"), xy(0, 0), xy(1000, 0)),
                pattern(1, "S1", List.of("B", "A"), xy(1000, 0), xy(1000, 150), xy(0, 150), xy(0, 0))),
            List.of(new Branch(0, List.of("A", "B")), new Branch(1, List.of("B", "A"))),
            STOPS, Rules.STREETS);

        assertThat(result.sections()).hasSize(2);
        assertThat(result.sections()).extracting(Section::directionIds).containsExactly(List.of(0), List.of(1));
        assertThat(result.sections().get(1).fromStopId()).isEqualTo("B");
        assertThat(maxNorth(result.sections().get(1).shape())).isCloseTo(150, within(1.0));
    }

    @Test
    void aBusBothWaysAlongTheSameStreetSharesOneSection() {
        Result result = LineSections.build(List.of(
                pattern(0, "S0", List.of("A", "B"), xy(0, 0), xy(1000, 0)),
                pattern(1, "S1", List.of("B", "A"), xy(1000, 0), xy(0, 0))),
            List.of(new Branch(0, List.of("A", "B")), new Branch(1, List.of("B", "A"))),
            STOPS, Rules.STREETS);

        assertThat(result.sections()).hasSize(1);
        assertThat(result.sections().getFirst().directionIds()).containsExactly(0, 1);
    }

    @Test
    void aShapeBesideTheTracksIsOutvoted() {
        // Like the line L at Suresnes: one shape 70 m beside the track the others follow, and the most frequent at that
        Result result = LineSections.build(List.of(
                pattern(0, "WRONG", List.of("A", "B"), xy(0, 0), xy(300, 70), xy(700, 70), xy(1000, 0)),
                pattern(0, "S0", List.of("A", "B", "C"), xy(0, 0), xy(2000, 0)),
                pattern(1, "S1", List.of("C", "B", "A"), xy(2000, 0), xy(0, 0)),
                pattern(1, "S2", List.of("B", "A"), xy(1000, 2), xy(0, 2))),
            List.of(new Branch(0, List.of("A", "B", "C")), new Branch(1, List.of("C", "B", "A"))),
            STOPS, Rules.TRACKS);

        assertThat(maxNorth(result.sections().getFirst().shape())).isLessThan(5);
    }

    @Test
    void ofTwoShapesAsCloseTheShorterOneIsKept() {
        // One shape each way, one of them wandering 300 m away: nothing to vote, the straighter one is the tracks
        Result result = LineSections.build(List.of(
                pattern(0, "WRONG", List.of("A", "B"), xy(0, 0), xy(500, 300), xy(1000, 0)),
                pattern(1, "RIGHT", List.of("B", "A"), xy(1000, 0), xy(0, 0))),
            List.of(new Branch(0, List.of("A", "B")), new Branch(1, List.of("B", "A"))),
            STOPS, Rules.TRACKS);

        assertThat(result.sections()).hasSize(1);
        assertThat(maxNorth(result.sections().getFirst().shape())).isLessThan(1);
    }

    @Test
    void aShapeGoingSomewhereAndBackLosesTheWayBack() {
        // Like IDFM's RER A and line L shapes: on to 1.5 km, back to 500 m on the other track, then on to the stop
        Result result = LineSections.build(List.of(
                pattern(0, "S0", List.of("A", "C"), xy(0, 0), xy(1500, 0), xy(1500, 4), xy(500, 4), xy(500, 0), xy(2000, 0))),
            List.of(new Branch(0, List.of("A", "C"))),
            STOPS, Rules.TRACKS);

        assertThat(length(result.sections().getFirst().shape())).isCloseTo(2000, within(30.0));
    }

    @Test
    void aBusRoundABlockKeepsItsLoop() {
        List<double[]> round = LineSections.withoutWaysBack(List.of(
            new double[]{0, 0}, new double[]{400, 0}, new double[]{400, 100}, new double[]{200, 100}, new double[]{200, 0},
            new double[]{1000, 0}));

        assertThat(lengthInMeters(round)).isCloseTo(1600, within(1.0));
    }

    @Test
    void aShapeMuchLongerThanTheStraightLineIsLeftOut() {
        // Both ways 4 km for stops 1 km apart: neither is believed, the section goes straight
        Result result = LineSections.build(List.of(
                pattern(0, "S0", List.of("A", "B"), xy(0, 0), xy(0, 1500), xy(1000, 1500), xy(1000, 0)),
                pattern(1, "S1", List.of("B", "A"), xy(1000, 0), xy(1000, 1500), xy(0, 1500), xy(0, 0))),
            List.of(new Branch(0, List.of("A", "B")), new Branch(1, List.of("B", "A"))),
            STOPS, Rules.TRACKS);

        assertThat(result.sections().getFirst().shape()).hasSize(2);
        assertThat(length(result.sections().getFirst().shape())).isCloseTo(1000, within(1.0));
    }

    @Test
    void aLoopRunOneWayGoesByItsOwnStops() {
        // Like the metro 10 at Auteuil: out by B, back by D
        Result result = LineSections.build(List.of(
                pattern(0, "S0", List.of("A", "B", "C"), xy(0, 0), xy(2000, 0)),
                pattern(1, "S1", List.of("C", "D", "A"), xy(2000, 0), xy(1000, -400), xy(0, 0))),
            List.of(new Branch(0, List.of("A", "B", "C")), new Branch(1, List.of("C", "D", "A"))),
            STOPS, Rules.TRACKS);

        assertThat(result.sections()).hasSize(4);
        assertThat(result.sections()).extracting(Section::directionIds)
            .containsExactly(List.of(0), List.of(0), List.of(1), List.of(1));
    }

    @Test
    void withoutAShapeASectionGoesStraightFromStopToStop() {
        Result result = LineSections.build(List.of(pattern(0, null, List.of("A", "B"))),
            List.of(new Branch(0, List.of("A", "B"))), STOPS, Rules.STREETS);

        assertThat(result.sections().getFirst().shape()).usingElementComparator(LineSectionsTest::compare)
            .containsExactly(STOPS.get("A"), STOPS.get("B"));
    }

    @Test
    void whereEachWayEndsAtATerminusDoesNotPartThem() {
        // One way comes from a turning loop north of A, where A falls on it; the other ends 30 m south, at the platform
        Map<String, double[]> stops = Map.of("A", at(0, 30), "B", at(1000, 0));
        Result result = LineSections.build(List.of(
                pattern(0, "S0", List.of("A", "B"), xy(0, 60), xy(0, 0), xy(1000, 0)),
                pattern(1, "S1", List.of("B", "A"), xy(1000, 0), xy(0, 0))),
            List.of(new Branch(0, List.of("A", "B")), new Branch(1, List.of("B", "A"))),
            stops, Rules.STREETS);

        assertThat(result.sections()).hasSize(1);
    }

    @Test
    void sectionsMeetAtOnePointPerStop() {
        // Each way's shape ends 10 m apart at B
        Result result = LineSections.build(List.of(
                pattern(0, "S0", List.of("A", "B"), xy(0, 0), xy(990, 0)),
                pattern(0, "S1", List.of("B", "C"), xy(1010, 0), xy(2000, 0))),
            List.of(new Branch(0, List.of("A", "B", "C"))),
            STOPS, Rules.TRACKS);

        assertThat(compare(result.sections().get(0).shape().getLast(), result.sections().get(1).shape().getFirst())).isZero();
    }

    private static Pattern pattern(int direction, String shapeId, List<String> stops, double[]... points) {
        return new Pattern(direction, stops, shapeId, List.of(points));
    }

    /** {@code [lon, lat]} of a point [x] meters east and [y] meters north of the origin */
    private static double[] xy(double x, double y) {
        return at(x, y);
    }

    private static double[] at(double x, double y) {
        return new double[]{LON + x / (METERS_PER_DEGREE * Math.cos(Math.toRadians(LAT))), LAT + y / METERS_PER_DEGREE};
    }

    private static double maxNorth(List<double[]> lonLat) {
        return lonLat.stream().mapToDouble(p -> (p[1] - LAT) * METERS_PER_DEGREE).max().orElse(0);
    }

    private static double length(List<double[]> lonLat) {
        List<double[]> meters = new ArrayList<>();
        for (double[] p : lonLat) {
            meters.add(new double[]{(p[0] - LON) * METERS_PER_DEGREE * Math.cos(Math.toRadians(LAT)), (p[1] - LAT) * METERS_PER_DEGREE});
        }
        return lengthInMeters(meters);
    }

    private static double lengthInMeters(List<double[]> points) {
        double length = 0;
        for (int i = 1; i < points.size(); i++) {
            length += Math.hypot(points.get(i)[0] - points.get(i - 1)[0], points.get(i)[1] - points.get(i - 1)[1]);
        }
        return length;
    }

    /** Equal within a meter or so (coordinates are rounded to 1e-6 degree) */
    private static int compare(double[] a, double[] b) {
        return Math.abs(a[0] - b[0]) < 2e-5 && Math.abs(a[1] - b[1]) < 2e-5 ? 0 : 1;
    }
}
