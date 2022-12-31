package com.gdamiens.website.controller.object;

public abstract class Call {

    private String expectedDepartureTime;

    private String expectedArrivalTime;

    private String aimedDepartureTime;

    private String aimedArrivalTime;

    private String departureStatus;

    protected Call(String expectedDepartureTime, String expectedArrivalTime, String aimedDepartureTime, String aimedArrivalTime, String departureStatus) {
        this.expectedDepartureTime = expectedDepartureTime;
        this.expectedArrivalTime = expectedArrivalTime;
        this.aimedDepartureTime = aimedDepartureTime;
        this.aimedArrivalTime = aimedArrivalTime;
        this.departureStatus = departureStatus;
    }

    public String getExpectedDepartureTime() {
        return expectedDepartureTime;
    }

    public void setExpectedDepartureTime(String expectedDepartureTime) {
        this.expectedDepartureTime = expectedDepartureTime;
    }

    public String getExpectedArrivalTime() {
        return expectedArrivalTime;
    }

    public void setExpectedArrivalTime(String expectedArrivalTime) {
        this.expectedArrivalTime = expectedArrivalTime;
    }

    public String getAimedDepartureTime() {
        return aimedDepartureTime;
    }

    public void setAimedDepartureTime(String aimedDepartureTime) {
        this.aimedDepartureTime = aimedDepartureTime;
    }

    public String getAimedArrivalTime() {
        return aimedArrivalTime;
    }

    public void setAimedArrivalTime(String aimedArrivalTime) {
        this.aimedArrivalTime = aimedArrivalTime;
    }

    public String getDepartureStatus() {
        return departureStatus;
    }

    public void setDepartureStatus(String departureStatus) {
        this.departureStatus = departureStatus;
    }
}
