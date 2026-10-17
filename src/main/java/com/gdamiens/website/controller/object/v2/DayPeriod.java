package com.gdamiens.website.controller.object.v2;

/**
 * Periods of a service day (Europe/Paris), over which the frequency of a line is told: before 7:00, morning peak
 * (7:00 to 10:00), daytime (10:00 to 16:00), evening peak (16:00 to 20:00), evening (20:00 to midnight), night
 * (after midnight, on the same service day)
 */
public enum DayPeriod {
    EARLY_MORNING(0, 7),
    MORNING_PEAK(7, 10),
    DAYTIME(10, 16),
    EVENING_PEAK(16, 20),
    EVENING(20, 24),
    NIGHT(24, 48);

    /** Hours of the service day (past 24 after midnight) */
    private final int startHour;

    private final int endHour;

    DayPeriod(int startHour, int endHour) {
        this.startHour = startHour;
        this.endHour = endHour;
    }

    public int startSeconds() {
        return startHour * 3600;
    }

    public int endSeconds() {
        return endHour * 3600;
    }

    /** @param seconds time of the service day, past 24:00 after midnight */
    public static DayPeriod of(int seconds) {
        for (DayPeriod period : values()) {
            if (seconds < period.endSeconds()) {
                return period;
            }
        }
        return NIGHT;
    }
}
