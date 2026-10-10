package com.gdamiens.website.client;

import com.gdamiens.website.client.siri.ArrivalPlatformName;
import com.gdamiens.website.client.siri.DestinationDisplay;
import com.gdamiens.website.client.siri.DestinationName;
import com.gdamiens.website.client.siri.DirectionName;
import com.gdamiens.website.client.siri.JourneyNote;
import com.gdamiens.website.client.siri.LineRef;
import com.gdamiens.website.client.siri.MonitoredCall;
import com.gdamiens.website.client.siri.MonitoredStopVisit;
import com.gdamiens.website.client.siri.MonitoredVehicleJourney;
import com.gdamiens.website.client.siri.VehicleJourneyName;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A vehicle expected at a stop area, from a PRIM stop-monitoring visit (SIRI Lite {@code MonitoredStopVisit})
 *
 * @param lineId             IDFM line id ({@code C01371}), from {@code STIF:Line::C01371:}
 * @param destination        destination name, else its display, else the direction
 * @param platform           null when unknown
 * @param vehicleJourneyName SNCF train number or mission code
 * @param journeyNote        SNCF mission code on some lines
 */
public record StopCall(String lineId, String destination, Instant aimedDeparture, Instant expectedDeparture,
                       Instant aimedArrival, Instant expectedArrival, String departureStatus, String arrivalStatus,
                       String platform, Boolean vehicleAtStop, String vehicleJourneyName, String journeyNote) {

    /** Expected departure, else arrival, else the aimed ones; null without any time */
    public Instant departureTime() {
        return firstNonNull(expectedDeparture, expectedArrival, aimedDeparture, aimedArrival);
    }

    /** Expected arrival, else departure, else the aimed ones; null without any time */
    public Instant arrivalTime() {
        return firstNonNull(expectedArrival, expectedDeparture, aimedArrival, aimedDeparture);
    }

    /** Aimed departure, else arrival */
    public Instant aimedTime() {
        return firstNonNull(aimedDeparture, aimedArrival);
    }

    /** @return null for a visit without line */
    public static StopCall from(MonitoredStopVisit visit) {
        MonitoredVehicleJourney journey = visit == null ? null : visit.getMonitoredVehicleJourney();
        String lineId = Optional.ofNullable(journey).map(MonitoredVehicleJourney::getLineRef).map(LineRef::getValue)
            .map(StopCall::lineId).orElse(null);
        if (lineId == null) {
            return null;
        }
        MonitoredCall call = journey.getMonitoredCall();
        Optional<MonitoredCall> monitoredCall = Optional.ofNullable(call);

        String destinationDisplay = monitoredCall.map(MonitoredCall::getDestinationDisplay).orElse(List.of()).stream()
            .map(DestinationDisplay::getValue)
            .filter(Objects::nonNull)
            .collect(Collectors.joining(", "));
        String destination = firstNonBlank(first(journey.getDestinationName(), DestinationName::getValue), destinationDisplay,
            first(journey.getDirectionName(), DirectionName::getValue));

        return new StopCall(lineId, destination,
            monitoredCall.map(MonitoredCall::getAimedDepartureTime).map(StopCall::parse).orElse(null),
            monitoredCall.map(MonitoredCall::getExpectedDepartureTime).map(StopCall::parse).orElse(null),
            monitoredCall.map(MonitoredCall::getAimedArrivalTime).map(StopCall::parse).orElse(null),
            monitoredCall.map(MonitoredCall::getExpectedArrivalTime).map(StopCall::parse).orElse(null),
            monitoredCall.map(MonitoredCall::getDepartureStatus).orElse(null),
            monitoredCall.map(MonitoredCall::getArrivalStatus).orElse(null),
            monitoredCall.map(MonitoredCall::getArrivalPlatformName).map(ArrivalPlatformName::getValue).map(StopCall::platform).orElse(null),
            monitoredCall.map(MonitoredCall::getVehicleAtStop).orElse(null),
            first(journey.getVehicleJourneyName(), VehicleJourneyName::getValue),
            first(journey.getJourneyNote(), JourneyNote::getValue));
    }

    /** {@code STIF:Line::C01371:} → {@code C01371} */
    static String lineId(String siriLineRef) {
        return Stream.of(siriLineRef.split(":")).filter(StringUtils::isNotBlank).reduce((first, second) -> second).orElse(null);
    }

    /** "unknown" is sometimes given for no platform */
    private static String platform(String value) {
        String platform = StringUtils.trimToNull(value);
        return platform == null || platform.equalsIgnoreCase("unknown") ? null : platform;
    }

    private static <T> String first(List<T> values, Function<T, String> value) {
        return values == null || values.isEmpty() ? null : value.apply(values.getFirst());
    }

    private static String firstNonBlank(String... values) {
        return Stream.of(values).filter(StringUtils::isNotBlank).findFirst().orElse(null);
    }

    @SafeVarargs
    private static <T> T firstNonNull(T... values) {
        return Stream.of(values).filter(Objects::nonNull).findFirst().orElse(null);
    }

    private static Instant parse(String value) {
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
