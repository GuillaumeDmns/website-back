package com.gdamiens.website.controller.object;

import com.gdamiens.website.idfm.DepartureStatusEnum;

public abstract class Call {
    private String expectedArrivalTime;

    private String expectedDepartureTime;

    private DepartureStatusEnum departureStatus;

    protected Call(String expectedArrivalTime, String expectedDepartureTime, DepartureStatusEnum departureStatus) {
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

    public DepartureStatusEnum getDepartureStatus() {
        return departureStatus;
    }

    public void setDepartureStatus(DepartureStatusEnum departureStatus) {
        this.departureStatus = departureStatus;
    }
}
