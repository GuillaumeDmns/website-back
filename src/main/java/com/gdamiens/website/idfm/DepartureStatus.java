package com.gdamiens.website.idfm;

public enum DepartureStatus {
    ON_TIME("onTime"),
    EARLY("early"),
    DELAYED("delayed"),
    CANCELLED("cancelled"),
    ARRIVED("arrived"),
    DEPARTED("departed"),
    NOT_EXPECTED("notExpected"),
    NO_REPORT("noReport");

    public final String value;

    DepartureStatus(String value) {
        this.value = value;
    }
}
