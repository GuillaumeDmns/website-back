package com.gdamiens.website.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A small GTFS in the IDFM format, imported by the integration tests through the real import:
 * <ul>
 *     <li>Métro 1 ({@code C01371}): Châtelet, Gare de Lyon, Nation, every 10 min both ways;</li>
 *     <li>RER A ({@code C01742}): Châtelet → Vincennes then a branch to Fontenay (every 15 min) and one to Joinville
 *     (every 30 min), hourly express trains to Fontenay skipping Gare de Lyon and Vincennes, trains back from
 *     Fontenay; all in GTFS direction 0, like the real RER A;</li>
 *     <li>Bus 38 ({@code C02000}): Châtelet ↔ Gare de Lyon every 20 min, day and night;</li>
 *     <li>Noctilien N11 ({@code C03000}): Châtelet → Nation hourly after midnight (24:30 to 29:30);</li>
 *     <li>a transfer from Châtelet to Les Halles.</li>
 * </ul>
 * Every service runs every day from 2020 to 2099, the rail lines from 05:00 to 01:50 the next night.
 */
public final class MiniGtfs {

    public static final String CHATELET = "IDFM:70001";
    public static final String GARE_DE_LYON = "IDFM:70002";
    public static final String NATION = "IDFM:70003";
    public static final String VINCENNES = "IDFM:70004";
    public static final String FONTENAY = "IDFM:70005";
    public static final String JOINVILLE = "IDFM:70006";
    public static final String LES_HALLES = "IDFM:70007";

    public static final String METRO_1 = "C01371";
    public static final String RER_A = "C01742";
    public static final String BUS_38 = "C02000";
    public static final String NOCTILIEN_11 = "C03000";

    public record Stop(String id, String name, double lat, double lon) {
    }

    public static final Map<String, Stop> STOP_AREAS = new LinkedHashMap<>();

    static {
        for (Stop stop : List.of(
            new Stop(CHATELET, "Châtelet", 48.8584, 2.3470),
            new Stop(GARE_DE_LYON, "Gare de Lyon", 48.8443, 2.3730),
            new Stop(NATION, "Nation", 48.8484, 2.3959),
            new Stop(VINCENNES, "Vincennes", 48.8474, 2.4334),
            new Stop(FONTENAY, "Fontenay-sous-Bois", 48.8432, 2.4878),
            new Stop(JOINVILLE, "Joinville-le-Pont", 48.8213, 2.4637),
            new Stop(LES_HALLES, "Les Halles", 48.8622, 2.3464))) {
            STOP_AREAS.put(stop.id(), stop);
        }
    }

    private final Map<String, StringBuilder> files = new LinkedHashMap<>();

    private int tripCount;

    private MiniGtfs() {
    }

    /** The whole network */
    public static byte[] network() {
        return build(true, UnaryOperator.identity());
    }

    /** Only the buses: far fewer trips than {@link #network()}, an import refuses it as truncated */
    public static byte[] busesOnly() {
        return build(false, UnaryOperator.identity());
    }

    /** The whole network, with [change] applied to the files' text (file name → content) */
    public static byte[] network(UnaryOperator<Map<String, String>> change) {
        return build(true, change);
    }

    private static byte[] build(boolean rail, UnaryOperator<Map<String, String>> change) {
        MiniGtfs gtfs = new MiniGtfs();
        gtfs.write(rail);
        Map<String, String> contents = new LinkedHashMap<>();
        gtfs.files.forEach((name, content) -> contents.put(name, content.toString()));
        return zip(change.apply(contents));
    }

    private void write(boolean rail) {
        file("agency.txt", "agency_id,agency_name,agency_url,agency_timezone,agency_lang");
        row("agency.txt", "IDFM:Operator_100", "RATP", "https://www.ratp.fr", "Europe/Paris", "fr");
        row("agency.txt", "IDFM:1046", "RER", "https://www.transilien.com", "Europe/Paris", "fr");
        row("agency.txt", "IDFM:Operator_101", "Noctilien", "https://www.iledefrance-mobilites.fr", "Europe/Paris", "fr");

        file("routes.txt", "route_id,agency_id,route_short_name,route_long_name,route_desc,route_type,route_url,route_color,route_text_color");
        if (rail) {
            row("routes.txt", "IDFM:" + METRO_1, "IDFM:Operator_100", "1", "Métro 1", "", "1", "", "FFCD00", "000000");
            row("routes.txt", "IDFM:" + RER_A, "IDFM:1046", "A", "RER A", "", "2", "", "EB2132", "FFFFFF");
        }
        row("routes.txt", "IDFM:" + BUS_38, "IDFM:Operator_100", "38", "Bus 38", "", "3", "", "82C8E6", "000000");
        row("routes.txt", "IDFM:" + NOCTILIEN_11, "IDFM:Operator_101", "N11", "Noctilien N11", "", "3", "", "0E1E61", "FFFFFF");

        file("stops.txt", "stop_id,stop_code,stop_name,stop_desc,stop_lat,stop_lon,zone_id,stop_url,location_type,parent_station,wheelchair_boarding,platform_code");
        STOP_AREAS.values().forEach(stop -> row("stops.txt", stop.id(), "", stop.name(), "", coordinate(stop.lat()), coordinate(stop.lon()),
            "1", "", "1", "", "1", ""));
        quay("IDFM:1001", CHATELET, "1", "1", "");
        quay("IDFM:1002", CHATELET, "1", "1", "A");
        quay("IDFM:1003", CHATELET, "1", "0", "");
        quay("IDFM:2001", GARE_DE_LYON, "1", "1", "");
        quay("IDFM:2002", GARE_DE_LYON, "1", "1", "A");
        quay("IDFM:2003", GARE_DE_LYON, "1", "1", "");
        quay("IDFM:3001", NATION, "1", "1", "");
        quay("IDFM:3002", NATION, "1", "1", "");
        quay("IDFM:3003", NATION, "1", "1", "");
        quay("IDFM:4002", VINCENNES, "1", "1", "");
        quay("IDFM:5002", FONTENAY, "1", "1", "");
        quay("IDFM:6002", JOINVILLE, "1", "1", "");
        quay("IDFM:7001", LES_HALLES, "1", "1", "");

        file("transfers.txt", "from_stop_id,to_stop_id,transfer_type,min_transfer_time");
        row("transfers.txt", "IDFM:1003", "IDFM:7001", "2", "240");
        row("transfers.txt", "IDFM:7001", "IDFM:1003", "2", "240");

        file("calendar.txt", "service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date");
        row("calendar.txt", "ALL", "1", "1", "1", "1", "1", "1", "1", "20200101", "20991231");
        file("calendar_dates.txt", "service_id,date,exception_type");
        row("calendar_dates.txt", "ALL", "20991225", "2");

        file("trips.txt", "route_id,service_id,trip_id,trip_headsign,trip_short_name,direction_id,shape_id");
        file("stop_times.txt", "trip_id,arrival_time,departure_time,stop_id,stop_sequence,pickup_type,drop_off_type,stop_headsign");
        file("shapes.txt", "shape_id,shape_pt_lat,shape_pt_lon,shape_pt_sequence");

        int fiveAm = 5 * 3600;
        int lastDeparture = 25 * 3600 + 50 * 60;
        if (rail) {
            trips(METRO_1, "M1-E", 0, "Nation", List.of("IDFM:1001", "IDFM:2001", "IDFM:3001"), 120, fiveAm, lastDeparture, 600, false);
            trips(METRO_1, "M1-W", 1, "Châtelet", List.of("IDFM:3001", "IDFM:2001", "IDFM:1001"), 120, fiveAm, lastDeparture, 600, false);
            trips(RER_A, "A-F", 0, "Fontenay-sous-Bois", List.of("IDFM:1002", "IDFM:2002", "IDFM:3002", "IDFM:4002", "IDFM:5002"),
                180, fiveAm, lastDeparture, 900, true);
            trips(RER_A, "A-J", 0, "Joinville-le-Pont", List.of("IDFM:1002", "IDFM:2002", "IDFM:3002", "IDFM:4002", "IDFM:6002"),
                180, fiveAm + 300, lastDeparture, 1800, true);
            trips(RER_A, "A-X", 0, "Fontenay-sous-Bois", List.of("IDFM:1002", "IDFM:3002", "IDFM:5002"), 240, fiveAm + 600, lastDeparture,
                3600, true);
            // One train fewer than towards Fontenay, so that the eastbound pattern is the reference direction
            trips(RER_A, "A-W", 0, "Châtelet", List.of("IDFM:5002", "IDFM:4002", "IDFM:3002", "IDFM:2002", "IDFM:1002"), 180,
                fiveAm + 600, lastDeparture, 900, true);
        }
        // Day and night (03:00 to 02:40 the next night): a scheduled departure within 2 h whenever the tests run
        trips(BUS_38, "38-E", 0, "Gare de Lyon", List.of("IDFM:1003", "IDFM:2003"), 600, 3 * 3600, 26 * 3600 + 2400, 1200, false);
        trips(BUS_38, "38-W", 1, "Châtelet", List.of("IDFM:2003", "IDFM:1003"), 600, 3 * 3600, 26 * 3600 + 2400, 1200, false);
        trips(NOCTILIEN_11, "N11", 0, "Nation", List.of("IDFM:1003", "IDFM:3003"), 900, 24 * 3600 + 1800, 29 * 3600 + 1800, 3600, false);
    }

    private void quay(String id, String stopArea, String zone, String wheelchair, String platform) {
        Stop parent = STOP_AREAS.get(stopArea);
        row("stops.txt", id, "", parent.name(), "", coordinate(parent.lat() + 0.0001), coordinate(parent.lon() + 0.0001), zone, "", "0",
            stopArea, wheelchair, platform);
    }

    /** Trips every [headway] s from [first] to [last], [hop] s between stops, along a shape through the stops */
    private void trips(String lineId, String pattern, int direction, String headsign, List<String> quays, int hop, int first, int last,
                       int headway, boolean numbered) {
        String shapeId = "SH-" + pattern;
        List<double[]> points = new ArrayList<>();
        for (String quay : quays) {
            Stop stop = STOP_AREAS.get(stopAreaOf(quay));
            if (!points.isEmpty()) {
                double[] previous = points.getLast();
                points.add(new double[]{(previous[0] + stop.lat()) / 2 + 0.002, (previous[1] + stop.lon()) / 2});
            }
            points.add(new double[]{stop.lat(), stop.lon()});
        }
        for (int i = 0; i < points.size(); i++) {
            row("shapes.txt", shapeId, coordinate(points.get(i)[0]), coordinate(points.get(i)[1]), String.valueOf(i + 1));
        }

        for (int start = first; start <= last; start += headway) {
            tripCount++;
            String tripId = "IDFM:TRIP-" + pattern + "-" + start;
            row("trips.txt", "IDFM:" + lineId, "ALL", tripId, headsign, numbered ? String.valueOf(150000 + tripCount) : "",
                String.valueOf(direction), shapeId);
            for (int s = 0; s < quays.size(); s++) {
                String time = time(start + s * hop);
                row("stop_times.txt", tripId, time, time, quays.get(s), String.valueOf(s + 1),
                    s == quays.size() - 1 ? "1" : "0", s == 0 ? "1" : "0", "");
            }
        }
    }

    /** Quays are numbered after their stop area: {@code IDFM:2003} is a quay of {@code IDFM:70002} */
    private static String stopAreaOf(String quayId) {
        return "IDFM:7000" + quayId.charAt("IDFM:".length());
    }

    private void file(String name, String header) {
        files.put(name, new StringBuilder(header).append('\n'));
    }

    private void row(String file, String... values) {
        files.get(file).append(String.join(",", values)).append('\n');
    }

    private static String coordinate(double value) {
        return String.format(java.util.Locale.ROOT, "%.6f", value);
    }

    /** {@code HH:MM:SS}, hours past 24 for the night after the service day */
    private static String time(int seconds) {
        return "%02d:%02d:%02d".formatted(seconds / 3600, seconds % 3600 / 60, seconds % 60);
    }

    private static byte[] zip(Map<String, String> contents) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (Map.Entry<String, String> file : contents.entrySet()) {
                zip.putNextEntry(new ZipEntry(file.getKey()));
                zip.write(file.getValue().getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes.toByteArray();
    }
}
