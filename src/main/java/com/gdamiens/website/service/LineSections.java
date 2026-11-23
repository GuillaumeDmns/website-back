package com.gdamiens.website.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * A line's path cut into sections between consecutive stops, each drawn once. The GTFS gives a shape per trip pattern:
 * both directions run side by side, branches overlap along their trunk, and some shapes are wrong (IDFM's line L towards
 * Paris, November 2026: out-and-back detours, tens of meters beside the tracks).
 * <p>
 * Each pair of consecutive stops gets the cut between them of every pattern's shape running it, either way. A cut
 * running somewhere and back over its own way loses that part (IDFM shapes of whole directions do so: the RER A's
 * between Nation and Gare de Lyon, 17.8 km for 2). Cuts much longer than the shortest ones or than the straight line are
 * detours and left out; among the others the one closest to all of them wins (a medoid), so that a wrong shape is
 * outvoted by the right ones, and between two that agree as much the shorter one. On tracks, both directions always
 * share one section between two stops (a one-way loop goes by other stops: the metro 10 at Auteuil); in streets they
 * share one when their best cuts stay close, otherwise each way has its own (a bus on one-way streets). Sections meet at
 * one point per stop, and a branch's path is made of its sections: vehicles placed along it stay on what is drawn.
 */
final class LineSections {

    /** A stop missing from a shape by more than this: the cut is not taken from that shape */
    private static final double MAX_STOP_DISTANCE = 300;

    /** A cut longer than this ratio of the shortest one running the same way (plus the slack) is a detour */
    private static final double DETOUR_RATIO = 1.5;

    private static final double DETOUR_SLACK = 100;

    /** ... or than this ratio of the shortest one either way (a bus's way back may go round a block, not this far) */
    private static final double OTHER_WAY_RATIO = 2.5;

    private static final double OTHER_WAY_SLACK = 500;

    /** A path back within this distance of where it was ... */
    private static final double REVISIT_METERS = 20;

    /** ... at least this far before, retracing its way, went there and back for nothing */
    private static final double REVISIT_AFTER = 200;

    /** Mean distance under which a way back retraces the way there */
    private static final double RETRACE_METERS = 25;

    /** Points of the way there compared with the way back (a quarter of them) */
    private static final int RETRACE_SAMPLES = 64;

    /** Ways back tried for a point coming back near where it was: the earliest ones */
    private static final int RETRACE_TRIES = 3;

    /** Points at most this far apart when looking for ways back */
    private static final double DENSIFY_METERS = 10;

    /** Points compared along each cut */
    private static final int SAMPLES = 16;

    /** Scores within this ratio of each other are as good (two cuts: the same sum of distances, up to rounding) */
    private static final double SCORE_TIE = 0.01;

    /** Part of each end left out when comparing both ways: where a stop falls on each shape differs at a terminus loop */
    private static final double END_MARGIN = 0.15;

    private static final double METERS_PER_DEGREE = 6_371_000 * Math.PI / 180;

    /**
     * A trip pattern, oriented
     *
     * @param shape {@code [lon, lat]} points, empty if the GTFS has none
     */
    record Pattern(int directionId, List<String> stopIds, String shapeId, List<double[]> shape) {
    }

    /**
     * How a kind of line is cut
     *
     * @param waysApart     whether each way may have its own section between two stops
     * @param sameWayMeters both ways share a section when their cuts stay this close
     * @param straightRatio a cut longer than this ratio of the straight line between its stops (plus the slack) is
     *                      wrong whatever the other cuts
     */
    record Rules(boolean waysApart, double sameWayMeters, double straightRatio, double straightSlack) {
        /** Metro and trains: one section on the tracks both ways; the metro 10's loop at Auteuil is 2.3 times its straight line */
        static final Rules TRACKS = new Rules(false, 0, 2.5, 100);
        /** Trams: both tracks in the same street, but some ways by other streets */
        static final Rules TRAM = new Rules(true, 50, 3, 1000);
        /** Buses: both ways on the same street points, one-way streets apart */
        static final Rules STREETS = new Rules(true, 25, 3, 1000);
    }

    /** A branch to cut into sections: its direction and its stop areas in order */
    record Branch(int directionId, List<String> stopIds) {
    }

    /**
     * @param directionIds directions of the branches running it ({@code from} to {@code to} or back)
     * @param shape        {@code [lon, lat]} points from {@code from} to {@code to}
     */
    record Section(String fromStopId, String toStopId, List<Integer> directionIds, List<double[]> shape) {
    }

    /**
     * @param branchSections per branch (in the order given), the section between each of its stops and the next one
     * @param branchShapes   per branch, its path through its sections ({@code [lon, lat]})
     */
    record Result(List<Section> sections, List<List<Integer>> branchSections, List<List<double[]>> branchShapes) {
    }

    private final Map<String, double[]> stops = new HashMap<>();
    private final double kx;
    private final Rules rules;

    /** Cuts per pair of stops (key: the two ids in natural order), oriented from the first to the second */
    private final Map<String, List<Cut>> cutsByPair = new LinkedHashMap<>();

    private final Map<String, Choice> choices = new HashMap<>();
    private final List<Path> paths = new ArrayList<>();
    private final List<String[]> ends = new ArrayList<>();
    private final List<Set<Integer>> directions = new ArrayList<>();
    private final Map<String, Integer> sectionIndex = new HashMap<>();

    private LineSections(Map<String, double[]> stopPositions, Rules rules) {
        double meanLat = stopPositions.values().stream().mapToDouble(p -> p[1]).average().orElse(48.85);
        this.kx = METERS_PER_DEGREE * Math.cos(Math.toRadians(meanLat));
        this.rules = rules;
        stopPositions.forEach((id, position) -> stops.put(id, toMeters(position)));
    }

    /**
     * @param stopPositions {@code [lon, lat]} of every stop of the patterns and branches
     */
    static Result build(List<Pattern> patterns, List<Branch> branches, Map<String, double[]> stopPositions, Rules rules) {
        LineSections sections = new LineSections(stopPositions, rules);
        sections.cutPatterns(patterns);
        return sections.assemble(patterns, branches);
    }

    private void cutPatterns(List<Pattern> patterns) {
        Set<String> seen = new HashSet<>();
        for (Pattern pattern : patterns) {
            Path shape = path(pattern.shape());
            if (shape == null) {
                continue;
            }
            // Each stop projected after the one before, so that a shape passing a place twice is cut in order
            double[] alongs = new double[pattern.stopIds().size()];
            double minAlong = 0;
            for (int i = 0; i < alongs.length; i++) {
                double[] stop = stops.get(pattern.stopIds().get(i));
                Projection projection = stop == null ? null : shape.project(stop, minAlong);
                if (projection == null || projection.distance() > MAX_STOP_DISTANCE) {
                    alongs[i] = Double.NaN;
                    continue;
                }
                alongs[i] = projection.along();
                minAlong = projection.along();
            }
            for (int i = 0; i + 1 < alongs.length; i++) {
                String from = pattern.stopIds().get(i);
                String to = pattern.stopIds().get(i + 1);
                if (Double.isNaN(alongs[i]) || Double.isNaN(alongs[i + 1]) || alongs[i + 1] <= alongs[i] || from.equals(to)) {
                    continue;
                }
                // The same shape cut between the same stops counts once, whatever the number of patterns sharing it
                if (seen.add(pattern.shapeId() + '|' + from + '|' + to)) {
                    addCut(from, to, shape.cut(alongs[i], alongs[i + 1]));
                }
            }
        }
    }

    private void addCut(String from, String to, List<double[]> points) {
        boolean forward = from.compareTo(to) < 0;
        List<double[]> straight = withoutWaysBack(points);
        Path path = new Path(forward ? straight : straight.reversed());
        if (path.length() > 0) {
            cutsByPair.computeIfAbsent(pairKey(from, to), k -> new ArrayList<>()).add(new Cut(path, forward));
        }
    }

    /**
     * The path without the parts it runs there and back over its own way: where it comes back near where it was
     * {@link #REVISIT_AFTER} meters before along the way it went, what lies between is dropped. A bus going round a
     * block comes back by another street and keeps its loop.
     */
    static List<double[]> withoutWaysBack(List<double[]> points) {
        List<double[]> dense = densify(points);
        int n = dense.size();
        // Points by their index in dense: along the kept path, their place in it, whether a way back dropped them
        double[] along = new double[n];
        int[] place = new int[n];
        boolean[] dropped = new boolean[n];
        int[] kept = new int[n];
        int size = 0;
        // Kept points by cell of REVISIT_METERS: those near a point are in its cell and the 8 around
        Map<Long, List<Integer>> grid = new HashMap<>();
        for (int p = 0; p < n; p++) {
            double[] point = dense.get(p);
            long cx = (long) Math.floor(point[0] / REVISIT_METERS);
            long cy = (long) Math.floor(point[1] / REVISIT_METERS);
            if (size > 0) {
                int last = kept[size - 1];
                double reached = along[last] + Path.distance(dense.get(last), point);
                List<Integer> near = new ArrayList<>();
                for (long dx = -1; dx <= 1; dx++) {
                    for (long dy = -1; dy <= 1; dy++) {
                        for (int q : grid.getOrDefault(cell(cx + dx, cy + dy), List.of())) {
                            if (!dropped[q] && along[q] <= reached - REVISIT_AFTER && Path.distance(dense.get(q), point) <= REVISIT_METERS) {
                                near.add(q);
                            }
                        }
                    }
                }
                // The earliest point it comes back to, the longest way there and back
                near.sort(null);
                for (int q : near.subList(0, Math.min(near.size(), RETRACE_TRIES))) {
                    List<double[]> loop = new ArrayList<>(size - place[q]);
                    for (int k = place[q]; k < size; k++) {
                        loop.add(dense.get(kept[k]));
                    }
                    if (retraces(loop, point)) {
                        for (int k = place[q] + 1; k < size; k++) {
                            dropped[kept[k]] = true;
                        }
                        size = place[q] + 1;
                        break;
                    }
                }
                along[p] = along[kept[size - 1]] + Path.distance(dense.get(kept[size - 1]), point);
            }
            place[p] = size;
            kept[size++] = p;
            grid.computeIfAbsent(cell(cx, cy), k -> new ArrayList<>()).add(p);
        }
        List<double[]> result = new ArrayList<>(size);
        for (int k = 0; k < size; k++) {
            result.add(dense.get(kept[k]));
        }
        return simplify(result);
    }

    private static long cell(long x, long y) {
        return x * 4_000_003L + y;
    }

    /** Douglas-Peucker within a meter: the points added to look for ways back go away */
    private static List<double[]> simplify(List<double[]> points) {
        if (points.size() < 3) {
            return points;
        }
        boolean[] keep = new boolean[points.size()];
        keep[0] = true;
        keep[points.size() - 1] = true;
        ArrayDeque<int[]> ranges = new ArrayDeque<>();
        ranges.push(new int[]{0, points.size() - 1});
        while (!ranges.isEmpty()) {
            int[] range = ranges.pop();
            Path chord = new Path(List.of(points.get(range[0]), points.get(range[1])));
            int farthest = -1;
            double max = 1;
            for (int i = range[0] + 1; i < range[1]; i++) {
                double distance = chord.distanceTo(points.get(i));
                if (distance > max) {
                    max = distance;
                    farthest = i;
                }
            }
            if (farthest >= 0) {
                keep[farthest] = true;
                ranges.push(new int[]{range[0], farthest});
                ranges.push(new int[]{farthest, range[1]});
            }
        }
        List<double[]> simplified = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            if (keep[i]) {
                simplified.add(points.get(i));
            }
        }
        return simplified;
    }

    /** Whether going along {@code loop} then to {@code end} is going somewhere and back the same way */
    private static boolean retraces(List<double[]> loop, double[] end) {
        double[] start = loop.getFirst();
        int far = 0;
        for (int i = 1; i < loop.size(); i++) {
            if (Path.distance(start, loop.get(i)) > Path.distance(start, loop.get(far))) {
                far = i;
            }
        }
        if (far == 0) {
            return true;
        }
        // Compared on samples of each part: the loop may be kilometers long
        Path there = new Path(loop.subList(0, far + 1));
        there = new Path(List.of(there.samples(RETRACE_SAMPLES)));
        List<double[]> backPoints = new ArrayList<>(loop.subList(far, loop.size()));
        backPoints.add(end);
        double[][] back = new Path(backPoints).samples(RETRACE_SAMPLES / 4);
        return there.meanDistance(back) <= RETRACE_METERS;
    }

    private static List<double[]> densify(List<double[]> points) {
        List<double[]> dense = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            if (i > 0) {
                double[] a = points.get(i - 1);
                double[] b = points.get(i);
                int steps = (int) Math.ceil(Path.distance(a, b) / DENSIFY_METERS);
                for (int s = 1; s < steps; s++) {
                    dense.add(new double[]{a[0] + (b[0] - a[0]) * s / steps, a[1] + (b[1] - a[1]) * s / steps});
                }
            }
            dense.add(points.get(i));
        }
        return dense;
    }

    private Result assemble(List<Pattern> patterns, List<Branch> branches) {
        List<List<Integer>> branchSections = new ArrayList<>();
        for (Branch branch : branches) {
            List<Integer> indexes = new ArrayList<>();
            for (int i = 0; i + 1 < branch.stopIds().size(); i++) {
                int index = section(branch.stopIds().get(i), branch.stopIds().get(i + 1), patterns);
                directions.get(index).add(branch.directionId());
                indexes.add(index);
            }
            branchSections.add(indexes);
        }

        joinAtStops();

        List<Section> sections = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            sections.add(new Section(ends.get(i)[0], ends.get(i)[1], List.copyOf(new TreeSet<>(directions.get(i))), toLonLat(paths.get(i).points)));
        }

        List<List<double[]>> branchShapes = new ArrayList<>();
        for (int b = 0; b < branches.size(); b++) {
            List<String> stopIds = branches.get(b).stopIds();
            List<double[]> shape = new ArrayList<>();
            for (int i = 0; i < branchSections.get(b).size(); i++) {
                int index = branchSections.get(b).get(i);
                List<double[]> points = ends.get(index)[0].equals(stopIds.get(i)) ? paths.get(index).points : paths.get(index).points.reversed();
                shape.addAll(shape.isEmpty() ? points : points.subList(1, points.size()));
            }
            branchShapes.add(toLonLat(shape));
        }
        return new Result(sections, branchSections, branchShapes);
    }

    /** Index of the section a branch runs from {@code from} to {@code to}, made when first asked */
    private int section(String from, String to, List<Pattern> patterns) {
        String key = pairKey(from, to);
        Choice choice = choices.computeIfAbsent(key, k -> choose(k, from, to, patterns));
        boolean forward = from.compareTo(to) < 0;

        // Both ways share it, or only one way has a cut (taken for the other one too: better than a straight line)
        Path own = forward ? choice.forward() : choice.backward();
        if (choice.shared() != null || own == null) {
            Path shared = choice.shared() != null ? choice.shared() : forward ? choice.backward() : choice.forward();
            return sectionIndex.computeIfAbsent(key, k -> newSection(shared, forward ? from : to, forward ? to : from));
        }
        return sectionIndex.computeIfAbsent(from + '>' + to,
            k -> forward ? newSection(own, from, to) : newSection(new Path(own.points.reversed()), from, to));
    }

    private int newSection(Path path, String from, String to) {
        paths.add(path);
        ends.add(new String[]{from, to});
        directions.add(new TreeSet<>());
        return paths.size() - 1;
    }

    /** The cuts of a pair voted on; without any, those of every shape passing both stops in either order, else a straight line */
    private Choice choose(String key, String from, String to, List<Pattern> patterns) {
        double straight = stops.containsKey(from) && stops.containsKey(to) ? Path.distance(stops.get(from), stops.get(to)) : 0;
        List<Cut> cuts = plausible(cutsByPair.getOrDefault(key, List.of()), straight);
        if (cuts.isEmpty()) {
            cuts = plausible(looseCuts(from, to, patterns), straight);
        }
        if (cuts.isEmpty()) {
            String first = from.compareTo(to) < 0 ? from : to;
            String second = first.equals(from) ? to : from;
            return new Choice(new Path(List.of(stops.get(first), stops.get(second))), null, null);
        }
        return vote(cuts);
    }

    /** Without the detours: cuts much longer than the straight line, than the shortest one, or than the shortest running the same way */
    private List<Cut> plausible(List<Cut> cuts, double straight) {
        List<Cut> near = cuts.stream().filter(c -> c.path().length() <= straight * rules.straightRatio() + rules.straightSlack()).toList();
        double shortest = near.stream().mapToDouble(c -> c.path().length()).min().orElse(0);
        double shortestForward = near.stream().filter(Cut::forward).mapToDouble(c -> c.path().length()).min().orElse(0);
        double shortestBackward = near.stream().filter(c -> !c.forward()).mapToDouble(c -> c.path().length()).min().orElse(0);
        return near.stream()
            .filter(c -> c.path().length() <= shortest * OTHER_WAY_RATIO + OTHER_WAY_SLACK)
            .filter(c -> c.path().length() <= (c.forward() ? shortestForward : shortestBackward) * DETOUR_RATIO + DETOUR_SLACK)
            .toList();
    }

    /** Cuts between two stops no pattern runs in a row (a stop only express trains skip): any shape passing near both */
    private List<Cut> looseCuts(String from, String to, List<Pattern> patterns) {
        double[] a = stops.get(from);
        double[] b = stops.get(to);
        List<Cut> cuts = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Pattern pattern : patterns) {
            Path shape = seen.add(pattern.shapeId()) ? path(pattern.shape()) : null;
            if (shape == null || a == null || b == null) {
                continue;
            }
            Projection pa = shape.project(a, 0);
            Projection pb = shape.project(b, 0);
            if (pa.distance() > MAX_STOP_DISTANCE || pb.distance() > MAX_STOP_DISTANCE || pa.along() == pb.along()) {
                continue;
            }
            boolean ordered = pa.along() < pb.along();
            List<double[]> points = withoutWaysBack(ordered ? shape.cut(pa.along(), pb.along()) : shape.cut(pb.along(), pa.along()).reversed());
            Path path = new Path(from.compareTo(to) < 0 ? points : points.reversed());
            if (path.length() > 0) {
                cuts.add(new Cut(path, from.compareTo(to) < 0 == ordered));
            }
        }
        return cuts;
    }

    private Choice vote(List<Cut> all) {
        // Shapes often agree to the point between two stops: each geometry once, with the number of cuts having it
        Map<String, Integer> weights = new LinkedHashMap<>();
        Map<String, Cut> byGeometry = new HashMap<>();
        for (Cut cut : all) {
            StringBuilder key = new StringBuilder(cut.forward() ? "f" : "b");
            for (double[] point : cut.path().points) {
                key.append(Math.round(point[0])).append(',').append(Math.round(point[1])).append(';');
            }
            weights.merge(key.toString(), 1, Integer::sum);
            byGeometry.putIfAbsent(key.toString(), cut);
        }
        List<Cut> cuts = weights.keySet().stream().map(byGeometry::get).toList();
        int[] weight = weights.values().stream().mapToInt(Integer::intValue).toArray();

        int n = cuts.size();
        double[][][] samples = new double[n][][];
        for (int i = 0; i < n; i++) {
            samples[i] = cuts.get(i).path().samples(SAMPLES);
        }
        double[] score = new double[n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double distance = (cuts.get(j).path().meanDistance(samples[i]) + cuts.get(i).path().meanDistance(samples[j])) / 2;
                score[i] += distance * weight[j];
                score[j] += distance * weight[i];
            }
        }

        Integer forward = best(cuts, score, Boolean.TRUE);
        Integer backward = best(cuts, score, Boolean.FALSE);
        if (rules.waysApart() && forward != null && backward != null) {
            Path f = cuts.get(forward).path();
            Path b = cuts.get(backward).path();
            if (Math.max(f.maxDistance(b.innerSamples(2 * SAMPLES)), b.maxDistance(f.innerSamples(2 * SAMPLES))) > rules.sameWayMeters()) {
                return new Choice(null, f, b);
            }
        }
        return new Choice(cuts.get(best(cuts, score, null)).path(), forward == null ? null : cuts.get(forward).path(),
            backward == null ? null : cuts.get(backward).path());
    }

    /**
     * The cut closest to all the others, among those running this way (any way when null); the shorter one between
     * two as close (two cuts are always as close to each other)
     */
    private static Integer best(List<Cut> cuts, double[] score, Boolean forward) {
        Integer best = null;
        for (int i = 0; i < cuts.size(); i++) {
            if (forward != null && cuts.get(i).forward() != forward) {
                continue;
            }
            double tie = best == null ? 0 : Math.max(1, score[best] * SCORE_TIE);
            if (best == null || score[i] < score[best] - tie
                || score[i] <= score[best] + tie && cuts.get(i).path().length() < cuts.get(best).path().length()) {
                best = i;
            }
        }
        return best;
    }

    /** Sections meeting at a stop end on the same point: the mean of where they ended there */
    private void joinAtStops() {
        Map<String, double[]> sums = new HashMap<>();
        for (int i = 0; i < paths.size(); i++) {
            List<double[]> points = paths.get(i).points;
            for (int end = 0; end < 2; end++) {
                double[] point = points.get(end == 0 ? 0 : points.size() - 1);
                double[] sum = sums.computeIfAbsent(ends.get(i)[end], k -> new double[3]);
                sum[0] += point[0];
                sum[1] += point[1];
                sum[2]++;
            }
        }
        for (int i = 0; i < paths.size(); i++) {
            List<double[]> points = new ArrayList<>(paths.get(i).points);
            double[] first = sums.get(ends.get(i)[0]);
            double[] last = sums.get(ends.get(i)[1]);
            points.set(0, new double[]{first[0] / first[2], first[1] / first[2]});
            points.set(points.size() - 1, new double[]{last[0] / last[2], last[1] / last[2]});
            paths.set(i, new Path(points));
        }
    }

    private static String pairKey(String a, String b) {
        return a.compareTo(b) < 0 ? a + '|' + b : b + '|' + a;
    }

    private Path path(List<double[]> lonLat) {
        if (lonLat == null || lonLat.size() < 2) {
            return null;
        }
        List<double[]> points = new ArrayList<>(lonLat.size());
        for (double[] point : lonLat) {
            points.add(toMeters(point));
        }
        Path path = new Path(points);
        return path.length() > 0 ? path : null;
    }

    private double[] toMeters(double[] lonLat) {
        return new double[]{lonLat[0] * kx, lonLat[1] * METERS_PER_DEGREE};
    }

    private List<double[]> toLonLat(List<double[]> points) {
        List<double[]> lonLat = new ArrayList<>(points.size());
        for (double[] point : points) {
            lonLat.add(new double[]{round(point[0] / kx), round(point[1] / METERS_PER_DEGREE)});
        }
        return lonLat;
    }

    private static double round(double degrees) {
        return Math.round(degrees * 1e6) / 1e6;
    }

    private record Cut(Path path, boolean forward) {
    }

    /**
     * What a pair of stops is drawn with: one section both ways ({@code shared}), else the best cut of each way
     * ({@code forward}: from the first stop of the pair in natural order to the second)
     */
    private record Choice(Path shared, Path forward, Path backward) {
    }

    private record Projection(double along, double distance) {
    }

    /** A polyline in meters (x east, y north) */
    private static final class Path {
        final List<double[]> points;
        final double[] cumulative;

        Path(List<double[]> points) {
            this.points = points;
            this.cumulative = new double[points.size()];
            for (int i = 1; i < points.size(); i++) {
                cumulative[i] = cumulative[i - 1] + distance(points.get(i - 1), points.get(i));
            }
        }

        double length() {
            return cumulative.length == 0 ? 0 : cumulative[cumulative.length - 1];
        }

        /** Closest point to {@code q} at or after {@code minAlong} */
        Projection project(double[] q, double minAlong) {
            Projection best = null;
            for (int i = 0; i + 1 < points.size(); i++) {
                double segment = cumulative[i + 1] - cumulative[i];
                if (cumulative[i + 1] < minAlong || segment == 0) {
                    continue;
                }
                double[] a = points.get(i);
                double[] b = points.get(i + 1);
                double t = ((q[0] - a[0]) * (b[0] - a[0]) + (q[1] - a[1]) * (b[1] - a[1])) / (segment * segment);
                double lowest = Math.max(0, (minAlong - cumulative[i]) / segment);
                t = Math.max(lowest, Math.min(1, t));
                double distance = distance(q, new double[]{a[0] + t * (b[0] - a[0]), a[1] + t * (b[1] - a[1])});
                if (best == null || distance < best.distance()) {
                    best = new Projection(cumulative[i] + t * segment, distance);
                }
            }
            return best;
        }

        double[] pointAt(double along) {
            if (along <= 0) {
                return points.getFirst();
            }
            for (int i = 0; i + 1 < points.size(); i++) {
                if (along <= cumulative[i + 1]) {
                    double segment = cumulative[i + 1] - cumulative[i];
                    double t = segment == 0 ? 0 : (along - cumulative[i]) / segment;
                    double[] a = points.get(i);
                    double[] b = points.get(i + 1);
                    return new double[]{a[0] + t * (b[0] - a[0]), a[1] + t * (b[1] - a[1])};
                }
            }
            return points.getLast();
        }

        /** The part from {@code from} to {@code to} meters along */
        List<double[]> cut(double from, double to) {
            List<double[]> part = new ArrayList<>();
            part.add(pointAt(from));
            for (int i = 0; i < points.size(); i++) {
                if (cumulative[i] > from && cumulative[i] < to) {
                    part.add(points.get(i));
                }
            }
            part.add(pointAt(to));
            return part;
        }

        double[][] samples(int count) {
            double[][] samples = new double[count][];
            for (int i = 0; i < count; i++) {
                samples[i] = pointAt(length() * i / (count - 1));
            }
            return samples;
        }

        /** Samples without the {@link #END_MARGIN} of each end */
        double[][] innerSamples(int count) {
            double[][] samples = new double[count][];
            double from = length() * END_MARGIN;
            double span = length() * (1 - 2 * END_MARGIN);
            for (int i = 0; i < count; i++) {
                samples[i] = pointAt(from + span * i / (count - 1));
            }
            return samples;
        }

        double distanceTo(double[] q) {
            double best = Double.MAX_VALUE;
            for (int i = 0; i + 1 < points.size(); i++) {
                double[] a = points.get(i);
                double[] b = points.get(i + 1);
                double dx = b[0] - a[0];
                double dy = b[1] - a[1];
                double squared = dx * dx + dy * dy;
                double t = squared == 0 ? 0 : Math.max(0, Math.min(1, ((q[0] - a[0]) * dx + (q[1] - a[1]) * dy) / squared));
                double ex = q[0] - a[0] - t * dx;
                double ey = q[1] - a[1] - t * dy;
                best = Math.min(best, ex * ex + ey * ey);
            }
            return Math.sqrt(best);
        }

        double meanDistance(double[][] samples) {
            double sum = 0;
            for (double[] sample : samples) {
                sum += distanceTo(sample);
            }
            return sum / samples.length;
        }

        double maxDistance(double[][] samples) {
            double max = 0;
            for (double[] sample : samples) {
                max = Math.max(max, distanceTo(sample));
            }
            return max;
        }

        private static double distance(double[] a, double[] b) {
            double dx = a[0] - b[0];
            double dy = a[1] - b[1];
            return Math.sqrt(dx * dx + dy * dy);
        }
    }
}
