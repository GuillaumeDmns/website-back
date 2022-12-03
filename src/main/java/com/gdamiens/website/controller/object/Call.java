package com.gdamiens.website.controller.object;

import com.gdamiens.website.idfm.DepartureStatusEnum;
import com.gdamiens.website.idfm.EstimatedCall;
import com.gdamiens.website.idfm.FirstOrLastJourneyEnum;

public class Call {

    private String recordedAtTime;

    private String expectedArrivalTime;

    private String expectedDepartureTime;

    private String aimedArrivalTime;

    private String aimedDepartureTime;

    private String arrivalPlatformName;

    private DepartureStatusEnum departureStatus;

    private String directionName;

    private String journeyNote;

    private FirstOrLastJourneyEnum firstOrLastJourney;

    public Call(EstimatedCall estimatedCall) {
        this.recordedAtTime = estimatedCall.getRecordedAtTime();
        this.expectedArrivalTime = estimatedCall.getExpectedArrivalTime();
        this.expectedDepartureTime = estimatedCall.getExpectedDepartureTime();
        this.aimedArrivalTime = estimatedCall.getAimedArrivalTime();
        this.aimedDepartureTime = estimatedCall.getAimedDepartureTime();
        this.arrivalPlatformName = estimatedCall.getArrivalPlatformName().getValue();
        this.departureStatus = estimatedCall.getDepartureStatus();
        this.directionName = estimatedCall.getDestinationDisplay().get(0).getValue();
        this.journeyNote = estimatedCall.getJourneyNote();
        this.firstOrLastJourney = estimatedCall.getFirstOrLastJourney();
    }

    public String getRecordedAtTime() {
        return recordedAtTime;
    }

    public void setRecordedAtTime(String recordedAtTime) {
        this.recordedAtTime = recordedAtTime;
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

    public String getAimedArrivalTime() {
        return aimedArrivalTime;
    }

    public void setAimedArrivalTime(String aimedArrivalTime) {
        this.aimedArrivalTime = aimedArrivalTime;
    }

    public String getAimedDepartureTime() {
        return aimedDepartureTime;
    }

    public void setAimedDepartureTime(String aimedDepartureTime) {
        this.aimedDepartureTime = aimedDepartureTime;
    }

    public String getArrivalPlatformName() {
        return arrivalPlatformName;
    }

    public void setArrivalPlatformName(String arrivalPlatformName) {
        this.arrivalPlatformName = arrivalPlatformName;
    }

    public DepartureStatusEnum getDepartureStatus() {
        return departureStatus;
    }

    public void setDepartureStatus(DepartureStatusEnum departureStatus) {
        this.departureStatus = departureStatus;
    }

    public String getDirectionName() {
        return directionName;
    }

    public void setDirectionName(String directionName) {
        this.directionName = directionName;
    }

    public String getJourneyNote() {
        return journeyNote;
    }

    public void setJourneyNote(String journeyNote) {
        this.journeyNote = journeyNote;
    }

    public FirstOrLastJourneyEnum getFirstOrLastJourney() {
        return firstOrLastJourney;
    }

    public void setFirstOrLastJourney(FirstOrLastJourneyEnum firstOrLastJourney) {
        this.firstOrLastJourney = firstOrLastJourney;
    }
}
