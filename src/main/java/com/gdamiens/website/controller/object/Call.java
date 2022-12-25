package com.gdamiens.website.controller.object;

public abstract class Call {
    private String expectedArrivalTime;

    private String expectedDepartureTime;

    private String departureStatus;

    protected Call(String expectedArrivalTime, String expectedDepartureTime, String departureStatus) {
        this.expectedArrivalTime = expectedArrivalTime;
        this.expectedDepartureTime = expectedDepartureTime;
        this.departureStatus = departureStatus;
    }

    public String getExpectedArrivalTime() {
        return expectedArrivalTime;
    }

    public void setExpectedArrivalTime(String expectedArrivalTime) {
        this.expectedArrivalTime = expectedArrivalTime;
    }

    public String getExpectedDepartureTime() {
        return expectedDepartureTime;
    }

    public void setExpectedDepartureTime(String expectedDepartureTime) {
        this.expectedDepartureTime = expectedDepartureTime;
    }

    public String getDepartureStatus() {
        return departureStatus;
    }

    public void setDepartureStatus(String departureStatus) {
        this.departureStatus = departureStatus;
    }
}
