package com.gdamiens.website.support;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/** PRIM real-time answers (SIRI Lite JSON) */
public final class SiriJson {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private SiriJson() {
    }

    /**
     * A vehicle expected at the monitored stop area
     *
     * @param lineId             IDFM line id ({@code C01742})
     * @param vehicleJourneyName train number or mission code, may be null
     */
    public record Visit(String lineId, String destination, Instant aimed, Instant expected, String platform, String journeyNote,
                        String vehicleJourneyName, String stopName) {

        public Visit(String lineId, String destination, Instant aimed, Instant expected, String platform, String journeyNote,
                     String vehicleJourneyName) {
            this(lineId, destination, aimed, expected, platform, journeyNote, vehicleJourneyName, null);
        }
    }

    /** A call of a vehicle journey at a quay ({@code IDFM:1002}) */
    public record Call(String quayId, Instant aimed, Instant expected) {
    }

    /**
     * @param destinationQuay quay ({@code IDFM:5002}) of the journey's destination, may be null
     */
    public record Journey(String id, String lineId, String destination, String destinationQuay, String name, List<Call> calls) {
    }

    public static String stopMonitoring(List<Visit> visits) {
        ObjectNode root = JSON.createObjectNode();
        ObjectNode delivery = root.putObject("Siri").putObject("ServiceDelivery").putArray("StopMonitoringDelivery").addObject();
        delivery.put("Status", "true");
        ArrayNode stopVisits = delivery.putArray("MonitoredStopVisit");
        for (Visit visit : visits) {
            ObjectNode journey = stopVisits.addObject().putObject("MonitoredVehicleJourney");
            journey.putObject("LineRef").put("value", "STIF:Line::" + visit.lineId() + ":");
            journey.putArray("DestinationName").addObject().put("value", visit.destination());
            if (visit.journeyNote() != null) {
                journey.putArray("JourneyNote").addObject().put("value", visit.journeyNote());
            }
            if (visit.vehicleJourneyName() != null) {
                journey.putArray("VehicleJourneyName").addObject().put("value", visit.vehicleJourneyName());
            }
            ObjectNode call = journey.putObject("MonitoredCall");
            if (visit.stopName() != null) {
                call.putArray("StopPointName").addObject().put("value", visit.stopName());
            }
            if (visit.aimed() != null) {
                call.put("AimedDepartureTime", visit.aimed().toString());
            }
            if (visit.expected() != null) {
                call.put("ExpectedDepartureTime", visit.expected().toString());
            }
            call.put("DepartureStatus", "onTime");
            if (visit.platform() != null) {
                call.putObject("ArrivalPlatformName").put("value", visit.platform());
            }
            call.put("VehicleAtStop", false);
        }
        return root.toString();
    }

    public static String estimatedTimetable(List<Journey> journeys) {
        ObjectNode root = JSON.createObjectNode();
        ArrayNode vehicleJourneys = root.putObject("Siri").putObject("ServiceDelivery").putArray("EstimatedTimetableDelivery").addObject()
            .putArray("EstimatedJourneyVersionFrame").addObject()
            .putArray("EstimatedVehicleJourney");
        for (Journey journey : journeys) {
            ObjectNode node = vehicleJourneys.addObject();
            node.putObject("LineRef").put("value", "STIF:Line::" + journey.lineId() + ":");
            node.putObject("DatedVehicleJourneyRef").put("value", journey.id());
            node.putArray("DestinationName").addObject().put("value", journey.destination());
            if (journey.destinationQuay() != null) {
                node.putObject("DestinationRef").put("value", siriQuay(journey.destinationQuay()));
            }
            if (journey.name() != null) {
                node.putArray("VehicleJourneyName").addObject().put("value", journey.name());
            }
            ArrayNode calls = node.putObject("EstimatedCalls").putArray("EstimatedCall");
            for (Call call : journey.calls()) {
                ObjectNode callNode = calls.addObject();
                callNode.putObject("StopPointRef").put("value", siriQuay(call.quayId()));
                callNode.putArray("DestinationDisplay").addObject().put("value", journey.destination());
                callNode.put("AimedArrivalTime", call.aimed().toString());
                callNode.put("ExpectedArrivalTime", call.expected().toString());
            }
        }
        return root.toString();
    }

    /** Text tags that IDFM's new real-time platform gives as one object instead of a list of one */
    private static final Set<String> SINGLE_OBJECT_TAGS = Set.of("DestinationName", "OriginName", "DestinationDisplay", "PublishedLineName",
        "DirectionName");

    /**
     * The same answer from IDFM's new real-time platform (announced on PRIM's Slack for the end of October 2026): text
     * tags as one object ({@code "DestinationName": {"value": …}}), with a published line name, an origin and a direction
     * of that shape, a new {@code ResponseMessageIdentifier}, the calls already made ({@code RecordedCalls}), and
     * without {@code RecordedAtTime} nor {@code ProductCategoryRef}.
     */
    public static String newPlatform(String answer) {
        JsonNode root = JSON.readTree(answer);
        JsonNode delivery = root.path("Siri").path("ServiceDelivery");
        for (String name : List.of("StopMonitoringDelivery", "EstimatedTimetableDelivery")) {
            for (JsonNode part : delivery.path(name).values()) {
                ((ObjectNode) part).put("ResponseMessageIdentifier", "IVTR_NG_HET:ResponseMessage::0a2f7c1e-5d1b-4f7e-9a51-1c2b3d4e5f60:LOC");
            }
        }
        for (JsonNode visit : delivery.path("StopMonitoringDelivery").path(0).path("MonitoredStopVisit").values()) {
            ((ObjectNode) visit).remove("RecordedAtTime");
            newJourney((ObjectNode) visit.path("MonitoredVehicleJourney"));
        }
        for (JsonNode journey : delivery.path("EstimatedTimetableDelivery").path(0).path("EstimatedJourneyVersionFrame").path(0)
            .path("EstimatedVehicleJourney").values()) {
            newJourney((ObjectNode) journey);
        }
        singleObjects(root);
        return root.toString();
    }

    private static void newJourney(ObjectNode journey) {
        journey.remove(List.of("RecordedAtTime", "ProductCategoryRef"));
        journey.putObject("PublishedLineName").put("value", "Ligne");
        journey.putObject("OriginName").put("value", "Origine");
        journey.putObject("DirectionName").put("value", "Direction");
        ObjectNode recorded = journey.putObject("RecordedCalls").putArray("RecordedCall").addObject();
        recorded.putObject("StopPointRef").put("value", "STIF:StopPoint:Q:9999:");
        recorded.put("Order", 1);
        recorded.put("AimedDepartureTime", "2026-10-10T07:50:00Z");
        recorded.put("ActualDepartureTime", "2026-10-10T07:51:00Z");
        recorded.put("DepartureStatus", "onTime");
        recorded.putObject("DeparturePlatformName").put("value", "1");
    }

    /** Lists of one {@code {"value": …}} object in [SINGLE_OBJECT_TAGS] become that object, anywhere */
    private static void singleObjects(JsonNode node) {
        if (node instanceof ObjectNode object) {
            for (String name : List.copyOf(object.propertyNames())) {
                JsonNode child = object.get(name);
                if (SINGLE_OBJECT_TAGS.contains(name) && child instanceof ArrayNode list && list.size() == 1) {
                    object.set(name, list.get(0));
                } else {
                    singleObjects(child);
                }
            }
        } else if (node instanceof ArrayNode list) {
            list.forEach(SiriJson::singleObjects);
        }
    }

    /** {@code IDFM:1002} → {@code STIF:StopPoint:Q:1002:} */
    private static String siriQuay(String quayId) {
        return "STIF:StopPoint:Q:" + quayId.substring(quayId.indexOf(':') + 1) + ":";
    }
}
