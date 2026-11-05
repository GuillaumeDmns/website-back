package com.gdamiens.website.client;

import com.gdamiens.website.client.siri.EstimatedVehicleJourney;
import com.gdamiens.website.client.siri.IDFMResponse;
import com.gdamiens.website.client.siri.MonitoredStopVisit;
import com.gdamiens.website.support.SiriJson;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StopCallTest {

    @Test
    void readsAStopMonitoringVisit() {
        Instant aimed = Instant.parse("2026-10-10T08:00:00Z");
        Instant expected = Instant.parse("2026-10-10T08:02:00Z");
        StopCall call = StopCall.from(visits(new SiriJson.Visit("C01742", "Marne-la-Vallée", aimed, expected, "2", "UPAC", "154023",
            "Châtelet-Les Halles")).getFirst());

        assertThat(call.lineId()).isEqualTo("C01742");
        assertThat(call.destination()).isEqualTo("Marne-la-Vallée");
        assertThat(call.aimedTime()).isEqualTo(aimed);
        assertThat(call.departureTime()).isEqualTo(expected);
        assertThat(call.arrivalTime()).isEqualTo(expected);
        assertThat(call.platform()).isEqualTo("2");
        assertThat(call.journeyPatternName()).isEqualTo("UPAC");
        assertThat(call.vehicleJourneyName()).isEqualTo("154023");
        assertThat(call.stopName()).isEqualTo("Châtelet-Les Halles");
    }

    @Test
    void readsTheNewRealTimePlatformFormat() {
        Instant aimed = Instant.parse("2026-10-10T08:00:00Z");
        Instant expected = Instant.parse("2026-10-10T08:02:00Z");
        String answer = SiriJson.newPlatform(SiriJson.stopMonitoring(List.of(new SiriJson.Visit("C01742", "Marne-la-Vallée", aimed, expected,
            "2", "UPAC", "154023", "Châtelet-Les Halles"))));
        assertThat(answer).contains("\"DestinationName\":{\"value\":\"Marne-la-Vallée\"}", "RecordedCalls", "IVTR_NG_HET:ResponseMessage::");

        IDFMResponse response = PrimRealtimeClient.SIRI_JSON.readValue(answer, IDFMResponse.class);
        StopCall call = StopCall.from(response.getSiri().getServiceDelivery().getStopMonitoringDelivery().getFirst().getMonitoredStopVisit()
            .getFirst());

        assertThat(call.lineId()).isEqualTo("C01742");
        assertThat(call.destination()).isEqualTo("Marne-la-Vallée");
        assertThat(call.departureTime()).isEqualTo(expected);
        assertThat(call.platform()).isEqualTo("2");
        assertThat(call.journeyPatternName()).isEqualTo("UPAC");
        assertThat(call.vehicleJourneyName()).isEqualTo("154023");
        assertThat(call.stopName()).isEqualTo("Châtelet-Les Halles");
    }

    @Test
    void fallsBackToTheAimedTimeAndIgnoresUnknownPlatforms() {
        Instant aimed = Instant.parse("2026-10-10T08:00:00Z");
        StopCall call = StopCall.from(visits(new SiriJson.Visit("C01371", "La Défense", aimed, null, "unknown", null, null)).getFirst());

        assertThat(call.departureTime()).isEqualTo(aimed);
        assertThat(call.platform()).isNull();
    }

    @Test
    void stillReadsTheFormerJourneyNoteMissionCode() {
        String answer = SiriJson.stopMonitoring(List.of(new SiriJson.Visit("C01742", "Marne-la-Vallée", Instant.parse("2026-10-10T08:00:00Z"),
            null, "2", "UPAC", null))).replace("JourneyPatternName", "JourneyNote");

        IDFMResponse response = PrimRealtimeClient.SIRI_JSON.readValue(answer, IDFMResponse.class);
        StopCall call = StopCall.from(response.getSiri().getServiceDelivery().getStopMonitoringDelivery().getFirst().getMonitoredStopVisit()
            .getFirst());

        assertThat(call.journeyPatternName()).isEqualTo("UPAC");
    }

    @Test
    void readsTheNewEstimatedTimetableTags() {
        String json = """
            {"RecordedAtTime":"2026-11-20T08:00:00Z","JourneyPatternRef":{"value":"JP:1"},
             "JourneyPatternName":[{"value":"ROMI"}],"OriginAimedDepartureTime":"2026-11-20T07:00:00Z",
             "DestinationAimedArrivalTime":"2026-11-20T09:00:00Z","VehicleMode":["rail"],
             "EstimatedCalls":{"EstimatedCall":[{"ArrivalBoardingActivity":"alighting","DepartureBoardingActivity":"boarding",
             "DepartureStatus":"departed"}]}}""";

        EstimatedVehicleJourney journey = PrimRealtimeClient.SIRI_JSON.readValue(json, EstimatedVehicleJourney.class);

        assertThat(journey.getJourneyPatternRef().getValue()).isEqualTo("JP:1");
        assertThat(journey.getJourneyPatternName().getFirst().getValue()).isEqualTo("ROMI");
        assertThat(journey.getOriginAimedDepartureTime()).isEqualTo("2026-11-20T07:00:00Z");
        assertThat(journey.getDestinationAimedArrivalTime()).isEqualTo("2026-11-20T09:00:00Z");
        assertThat(journey.getVehicleMode()).containsExactly("rail");
        assertThat(journey.getEstimatedCalls().getEstimatedCall().getFirst().getDepartureBoardingActivity()).isEqualTo("boarding");
        assertThat(journey.getEstimatedCalls().getEstimatedCall().getFirst().getArrivalBoardingActivity()).isEqualTo("alighting");
        assertThat(journey.getEstimatedCalls().getEstimatedCall().getFirst().getDepartureStatus()).isEqualTo("departed");
    }

    @Test
    void skipsVisitsWithoutLine() {
        assertThat(StopCall.from(new MonitoredStopVisit())).isNull();
        assertThat(StopCall.from(null)).isNull();
    }

    @Test
    void readsSiriLineRefs() {
        assertThat(StopCall.lineId("STIF:Line::C01371:")).isEqualTo("C01371");
        assertThat(StopCall.lineId("C01371")).isEqualTo("C01371");
    }

    private static List<MonitoredStopVisit> visits(SiriJson.Visit visit) {
        return PrimRealtimeClient.SIRI_JSON.readValue(SiriJson.stopMonitoring(List.of(visit)), IDFMResponse.class)
            .getSiri().getServiceDelivery().getStopMonitoringDelivery().getFirst().getMonitoredStopVisit();
    }
}
