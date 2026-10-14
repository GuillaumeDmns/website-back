package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.v2.DayPeriod;
import com.gdamiens.website.controller.object.v2.Frequency;
import com.gdamiens.website.controller.object.v2.LineBranch;
import com.gdamiens.website.controller.object.v2.LineDetail;
import com.gdamiens.website.controller.object.v2.LineDirection;
import com.gdamiens.website.controller.object.v2.LineSummary;
import com.gdamiens.website.controller.object.v2.ServiceDirection;
import com.gdamiens.website.controller.object.v2.StopRef;
import com.gdamiens.website.model.TransportMode;
import com.gdamiens.website.repository.NetworkRepository.TripCallRow;
import com.gdamiens.website.utils.ParisTime;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceHoursServiceTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 14);

    @Test
    void tellsTheFirstAndLastDeparturesAndTheFrequencyPerPeriod() {
        List<Instant> departures = new ArrayList<>();
        // Every 10 min from 5:30, every 4 min in the morning peak, every 10 min again, a last one at 0:40
        IntStream.iterate(5 * 3600 + 1800, s -> s < 7 * 3600, s -> s + 600).forEach(s -> departures.add(at(s)));
        IntStream.iterate(7 * 3600, s -> s < 10 * 3600, s -> s + 240).forEach(s -> departures.add(at(s)));
        IntStream.iterate(10 * 3600, s -> s <= 23 * 3600, s -> s + 600).forEach(s -> departures.add(at(s)));
        departures.add(at(24 * 3600 + 2400));

        ServiceHoursService.Summary summary = ServiceHoursService.summarize(DAY, departures).orElseThrow();

        assertThat(summary.first()).isEqualTo(Instant.parse("2026-10-14T03:30:00Z"));
        // After midnight, on the same service day
        assertThat(summary.last()).isEqualTo(Instant.parse("2026-10-14T22:40:00Z"));
        assertThat(summary.frequencies()).extracting(Frequency::period).containsExactly(DayPeriod.EARLY_MORNING, DayPeriod.MORNING_PEAK,
            DayPeriod.DAYTIME, DayPeriod.EVENING_PEAK, DayPeriod.EVENING, DayPeriod.NIGHT);

        Frequency early = summary.frequencies().getFirst();
        // From the first departure to the period's end
        assertThat(early.start()).isEqualTo(summary.first());
        assertThat(early.end()).isEqualTo(Instant.parse("2026-10-14T05:00:00Z"));
        assertThat(early.departures()).isEqualTo(9);
        assertThat(early.minMinutes()).isEqualTo(10);
        assertThat(early.maxMinutes()).isEqualTo(10);

        Frequency peak = summary.frequencies().get(1);
        assertThat(peak.start()).isEqualTo(Instant.parse("2026-10-14T05:00:00Z"));
        assertThat(peak.departures()).isEqualTo(45);
        assertThat(peak.minMinutes()).isEqualTo(4);
        assertThat(peak.maxMinutes()).isEqualTo(4);

        // The 100 min gap before the last departure is left out of the evening's usual range
        Frequency evening = summary.frequencies().get(4);
        assertThat(evening.minMinutes()).isEqualTo(10);
        assertThat(evening.maxMinutes()).isEqualTo(10);

        Frequency night = summary.frequencies().getLast();
        assertThat(night.departures()).isEqualTo(1);
        assertThat(night.minMinutes()).isNull();
        assertThat(night.end()).isEqualTo(summary.last());
    }

    @Test
    void givesTheUsualRangeOfIrregularGaps() {
        // 11:00, 11:06, 11:18, 11:26, 11:40, 11:41, 11:50: gaps of 6, 12, 8, 14, 1, 9 min
        List<Instant> departures = List.of(at(11, 0), at(11, 6), at(11, 18), at(11, 26), at(11, 40), at(11, 41), at(11, 50));

        Frequency daytime = ServiceHoursService.summarize(DAY, departures).orElseThrow().frequencies().getFirst();

        assertThat(daytime.period()).isEqualTo(DayPeriod.DAYTIME);
        assertThat(daytime.minMinutes()).isEqualTo(6);
        assertThat(daytime.maxMinutes()).isEqualTo(12);
        // Two departures the same minute (two branches) are one wait
        assertThat(ServiceHoursService.summarize(DAY, List.of(at(12, 0), at(12, 0), at(12, 15))).orElseThrow()
            .frequencies().getFirst().minMinutes()).isEqualTo(15);
    }

    @Test
    void hasNothingToTellWithoutDepartures() {
        assertThat(ServiceHoursService.summarize(DAY, List.of())).isEmpty();
    }

    @Test
    void countsEachDirectionAtItsBusiestStop() {
        // A → B → C → D one way, with short trips C → D; D → C → B → A the other way
        LineDetail detail = new LineDetail(new LineSummary("C1", "1", "Ligne 1", TransportMode.METRO, null, null), List.of(
            new LineDirection(0, List.of(new LineBranch("D", 10, stops("A", "B", "C", "D"), List.of()))),
            new LineDirection(1, List.of(new LineBranch("A", 10, stops("D", "C", "B", "A"), List.of())))));
        List<TripCallRow> calls = new ArrayList<>();
        trip(calls, "east-1", 6 * 3600, "A", "B", "C", "D");
        trip(calls, "east-2", 6 * 3600 + 600, "A", "B", "C", "D");
        trip(calls, "short-1", 6 * 3600 + 300, "C", "D");
        trip(calls, "short-2", 6 * 3600 + 900, "C", "D");
        // The same train under a second calendar
        trip(calls, "east-2-bis", 6 * 3600 + 600, "A", "B", "C", "D");
        trip(calls, "west-1", 7 * 3600, "D", "C", "B", "A");

        List<ServiceDirection> directions = ServiceHoursService.directions(detail, calls, DAY);

        assertThat(directions).hasSize(2);
        ServiceDirection east = directions.getFirst();
        assertThat(east.name()).isEqualTo("D");
        assertThat(east.direction()).isZero();
        // C sees the most trips
        assertThat(east.at().id()).isEqualTo("C");
        assertThat(east.first()).isEqualTo(ParisTime.ofServiceDay(DAY, 6 * 3600 + 120));
        assertThat(east.last()).isEqualTo(ParisTime.ofServiceDay(DAY, 6 * 3600 + 900));
        assertThat(east.frequencies().getFirst().departures()).isEqualTo(4);
        assertThat(east.frequencies().getFirst().minMinutes()).isEqualTo(3);

        // Every stop sees the one trip: its first one
        ServiceDirection west = directions.get(1);
        assertThat(west.at().id()).isEqualTo("D");
        assertThat(west.first()).isEqualTo(ParisTime.ofServiceDay(DAY, 7 * 3600));
    }

    private static Instant at(int seconds) {
        return ParisTime.ofServiceDay(DAY, seconds);
    }

    private static Instant at(int hour, int minute) {
        return at(hour * 3600 + minute * 60);
    }

    private static List<StopRef> stops(String... ids) {
        return java.util.Arrays.stream(ids).map(id -> new StopRef(id, id, 48.85, 2.35)).toList();
    }

    /** Calls 60 s apart from [start], no boarding at the last stop */
    private static void trip(List<TripCallRow> calls, String id, int start, String... stops) {
        for (int i = 0; i < stops.length; i++) {
            calls.add(new TripCallRow(id, stops[i], start + 60 * i, i < stops.length - 1));
        }
    }
}
