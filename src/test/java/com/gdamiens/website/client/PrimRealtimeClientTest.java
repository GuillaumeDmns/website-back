package com.gdamiens.website.client;

import com.gdamiens.website.client.siri.EstimatedVehicleJourney;
import com.gdamiens.website.support.SiriJson;
import com.gdamiens.website.support.SiriJson.Call;
import com.gdamiens.website.support.SiriJson.Journey;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** The whole network's estimated-timetable, split by line as it is read */
class PrimRealtimeClientTest {

    private final Instant now = Instant.parse("2026-11-22T18:00:00Z");

    private Journey journey(String id, String lineId) {
        return new Journey(id, lineId, "Nation", null, null, List.of(new Call("IDFM:3001", now, now.plusSeconds(60))));
    }

    private static Map<String, byte[]> split(String answer) {
        return PrimRealtimeClient.splitByLine(new ByteArrayInputStream(answer.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void groupsTheJourneysByLine() {
        Map<String, byte[]> lines = split(SiriJson.estimatedTimetable(List.of(
            journey("M1-1", "C01371"), journey("RERA-1", "C01742"), journey("M1-2", "C01371"))));

        assertThat(lines).containsOnlyKeys("C01371", "C01742");
        assertThat(PrimRealtimeClient.readJourneys(lines.get("C01371")))
            .extracting(journey -> journey.getDatedVehicleJourneyRef().getValue())
            .containsExactly("M1-1", "M1-2");
        List<EstimatedVehicleJourney> rer = PrimRealtimeClient.readJourneys(lines.get("C01742"));
        assertThat(rer).singleElement().satisfies(journey -> {
            assertThat(journey.getDestinationName()).singleElement().satisfies(name -> assertThat(name.getValue()).isEqualTo("Nation"));
            assertThat(journey.getEstimatedCalls().getEstimatedCall()).singleElement()
                .satisfies(call -> assertThat(call.getExpectedArrivalTime()).isNotNull());
        });
    }

    @Test
    void readsTheNewRealTimePlatformFormat() {
        Map<String, byte[]> lines = split(SiriJson.newPlatform(SiriJson.estimatedTimetable(List.of(journey("RERA-1", "C01742")))));

        assertThat(PrimRealtimeClient.readJourneys(lines.get("C01742"))).singleElement()
            .satisfies(journey -> assertThat(journey.getDestinationName()).singleElement()
                .satisfies(name -> assertThat(name.getValue()).isEqualTo("Nation")));
    }

    @Test
    void leavesOutJourneysWithoutLine() {
        String answer = SiriJson.estimatedTimetable(List.of(journey("M1-1", "C01371"), journey("none", "")));

        assertThat(split(answer)).containsOnlyKeys("C01371");
    }

    @Test
    void anAnswerWithoutJourneysHasNoLine() {
        assertThat(split(SiriJson.estimatedTimetable(List.of()))).isEmpty();
    }
}
