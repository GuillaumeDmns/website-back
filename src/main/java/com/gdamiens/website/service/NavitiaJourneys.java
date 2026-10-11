package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.v2.JourneyOption;
import com.gdamiens.website.controller.object.v2.JourneyPlan.PageCursor;
import com.gdamiens.website.controller.object.v2.JourneyPoint;
import com.gdamiens.website.controller.object.v2.JourneySection;
import com.gdamiens.website.controller.object.v2.JourneySection.Kind;
import com.gdamiens.website.controller.object.v2.JourneyStop;
import com.gdamiens.website.controller.object.v2.LineSummary;
import com.gdamiens.website.controller.object.v2.WalkStep;
import com.gdamiens.website.model.TransportMode;
import com.gdamiens.website.utils.ParisTime;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Navitia {@code journeys} responses (raw snake_case JSON) turned into the compact v2 model, with the GTFS lines
 */
final class NavitiaJourneys {

    private NavitiaJourneys() {
    }

    static JourneyOption toJourney(JsonNode journey, Map<String, LineSummary> lines) {
        List<JourneySection> sections = new ArrayList<>();
        for (JsonNode section : journey.path("sections").values()) {
            JourneySection mapped = toSection(section, lines);
            // Zero-length links between an address and a stop are noise
            if (mapped != null && !(mapped.kind() == Kind.WALK && mapped.duration() == 0)) {
                sections.add(mapped);
            }
        }

        JsonNode fare = journey.path("fare");
        Integer fareCents = fare.path("found").asBoolean(false) && "centime".equals(fare.path("total").path("currency").asString(""))
            ? (int) Math.round(fare.path("total").path("value").asDouble(0))
            : null;

        return new JourneyOption(
            journey.path("type").asString(null),
            journey.path("tags").values().stream().map(tag -> tag.asString("")).filter(StringUtils::isNotEmpty).toList(),
            ParisTime.parseNavitia(journey.path("departure_date_time").asString(null)),
            ParisTime.parseNavitia(journey.path("arrival_date_time").asString(null)),
            journey.path("duration").asInt(0),
            journey.path("nb_transfers").asInt(0),
            optionalInt(journey.path("durations").path("walking")),
            optionalInt(journey.path("distances").path("walking")),
            journey.path("co2_emission").path("value").isNumber() ? journey.path("co2_emission").path("value").asDouble() : null,
            fareCents,
            sections);
    }

    private static JourneySection toSection(JsonNode section, Map<String, LineSummary> lines) {
        String type = section.path("type").asString("");
        String mode = section.path("mode").asString("");
        Kind kind = switch (type) {
            case "public_transport", "on_demand_transport" -> Kind.TRANSIT;
            case "transfer" -> Kind.TRANSFER;
            case "waiting" -> Kind.WAIT;
            case "street_network", "crow_fly" -> switch (mode) {
                case "bike", "bss" -> Kind.BIKE;
                case "car", "ridesharing", "taxi" -> Kind.CAR;
                default -> Kind.WALK;
            };
            case "bss_rent", "bss_put_back", "park", "leave_parking", "alighting", "boarding" -> null;
            default -> Kind.OTHER;
        };
        if (kind == null) {
            return null;
        }

        Instant departure = ParisTime.parseNavitia(section.path("departure_date_time").asString(null));
        Instant arrival = ParisTime.parseNavitia(section.path("arrival_date_time").asString(null));

        LineSummary line = null;
        String headsign = null;
        List<String> boardingPositions = List.of();
        List<JourneyStop> stops = List.of();
        Boolean realtime = null;
        Integer delay = null;

        if (kind == Kind.TRANSIT) {
            JsonNode display = section.path("display_informations");
            line = lineOf(section, display, lines);
            headsign = removeTown(display.path("direction").asString(display.path("headsign").asString(null)));
            boardingPositions = section.path("best_boarding_positions").values().stream()
                .map(position -> position.asString("").toLowerCase(Locale.ROOT))
                .filter(StringUtils::isNotEmpty)
                .toList();
            stops = section.path("stop_date_times").values().stream()
                .map(stop -> {
                    JsonNode coord = stop.path("stop_point").path("coord");
                    Instant time = ParisTime.parseNavitia(stop.path("departure_date_time").asString(stop.path("arrival_date_time").asString(null)));
                    return new JourneyStop(stop.path("stop_point").path("name").asString(""),
                        coord.path("lat").asDouble(0), coord.path("lon").asDouble(0), time);
                })
                .toList();
            realtime = "realtime".equals(section.path("data_freshness").asString(""));
            Instant base = ParisTime.parseNavitia(section.path("base_departure_date_time").asString(null));
            if (realtime && base != null && departure != null) {
                delay = (int) (departure.getEpochSecond() - base.getEpochSecond());
            }
        }

        List<WalkStep> steps = section.path("path").values().stream()
            .filter(step -> StringUtils.isNotBlank(step.path("instruction").asString(null)))
            .map(step -> new WalkStep(step.path("instruction").asString(""), step.path("length").asInt(0), step.path("duration").asInt(0)))
            .toList();
        Integer length = optionalInt(section.path("geojson").path("properties").path(0).path("length"));

        List<double[]> shape = new ArrayList<>();
        for (JsonNode point : section.path("geojson").path("coordinates").values()) {
            shape.add(new double[]{point.path(0).asDouble(0), point.path(1).asDouble(0)});
        }

        return new JourneySection(kind, departure, arrival, section.path("duration").asInt(0),
            toPoint(section.path("from")), toPoint(section.path("to")),
            line, headsign, boardingPositions, stops, steps, realtime, delay, length, shape);
    }

    /** GTFS line of the section (consistent badges with the rest of the app), else built from Navitia's display */
    private static LineSummary lineOf(JsonNode section, JsonNode display, Map<String, LineSummary> lines) {
        for (JsonNode link : section.path("links").values()) {
            if ("line".equals(link.path("type").asString(""))) {
                LineSummary line = lines.get(StringUtils.substringAfterLast(link.path("id").asString(""), ":"));
                if (line != null) {
                    return line;
                }
            }
        }

        TransportMode mode = switch (display.path("physical_mode").asString("")) {
            case "Métro" -> TransportMode.METRO;
            case "RER" -> TransportMode.RER;
            case "Tramway" -> TransportMode.TRAM;
            case "Train Transilien" -> TransportMode.TRANSILIEN;
            case "TER / Intercités" -> TransportMode.TER;
            default -> TransportMode.BUS;
        };
        return new LineSummary(null, display.path("code").asString(null), display.path("name").asString(null), mode,
            display.path("color").asString(null), display.path("text_color").asString(null));
    }

    private static JourneyPoint toPoint(JsonNode place) {
        if (place.isMissingNode() || place.isNull()) {
            return null;
        }
        String embeddedType = place.path("embedded_type").asString("");
        JsonNode object = place.path(embeddedType);
        JsonNode coord = object.path("coord");

        String stopAreaId = switch (embeddedType) {
            case "stop_area" -> object.path("id").asString(null);
            case "stop_point" -> object.path("stop_area").path("id").asString(null);
            default -> null;
        };
        return new JourneyPoint(
            place.path("name").asString(""),
            coord.path("lat").asDouble(0),
            coord.path("lon").asDouble(0),
            stopAreaId == null ? null : StringUtils.removeStart(stopAreaId, "stop_area:"));
    }

    /** Navitia "next"/"prev" links carry the datetime to request */
    static PageCursor toCursor(String href) {
        if (href == null) {
            return null;
        }
        Map<String, List<String>> params = UriComponentsBuilder.fromUriString(href).build().getQueryParams();
        Instant datetime = ParisTime.parseNavitia(first(params.get("datetime")));
        return datetime == null ? null : new PageCursor(datetime, "arrival".equals(first(params.get("datetime_represents"))));
    }

    private static String first(List<String> values) {
        return values == null || values.isEmpty() ? null : values.getFirst();
    }

    /** {@code Saint-Denis - Pleyel (Saint-Denis)} → {@code Saint-Denis - Pleyel} */
    private static String removeTown(String direction) {
        return direction == null ? null : direction.replaceFirst("\\s*\\([^()]*\\)$", "");
    }

    private static Integer optionalInt(JsonNode node) {
        return node.isNumber() ? node.asInt() : null;
    }
}
