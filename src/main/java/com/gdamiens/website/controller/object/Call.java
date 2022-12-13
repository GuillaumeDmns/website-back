package com.gdamiens.website.controller.object;

import com.gdamiens.website.idfm.StatusEnum;

public abstract class Call {
    private String expectedArrivalTime;

    private String expectedDepartureTime;

    private StatusEnum departureStatus;

    protected Call(String expectedArrivalTime, String expectedDepartureTime, StatusEnum departureStatus) {
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

    public StatusEnum getDepartureStatus() {
        return departureStatus;
    }

    public void setDepartureStatus(StatusEnum departureStatus) {
        this.departureStatus = departureStatus;
    }
}
