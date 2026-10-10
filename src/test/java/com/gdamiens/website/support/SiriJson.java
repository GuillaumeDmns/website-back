package com.gdamiens.website.support;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.List;

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
                        String vehicleJourneyName) {
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
                callNode.put("AimedArrivalTime", call.aimed().toString());
                callNode.put("ExpectedArrivalTime", call.expected().toString());
            }
        }
        return root.toString();
    }

    /** {@code IDFM:1002} → {@code STIF:StopPoint:Q:1002:} */
    private static String siriQuay(String quayId) {
        return "STIF:StopPoint:Q:" + quayId.substring(quayId.indexOf(':') + 1) + ":";
    }
}
