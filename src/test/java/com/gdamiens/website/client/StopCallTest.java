package com.gdamiens.website.client;

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
        assertThat(call.journeyNote()).isEqualTo("UPAC");
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
        assertThat(call.journeyNote()).isEqualTo("UPAC");
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
