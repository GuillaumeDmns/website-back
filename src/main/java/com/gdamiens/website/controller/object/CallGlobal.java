package com.gdamiens.website.controller.object;

import com.gdamiens.website.idfm.ArrivalPlatformName;
import com.gdamiens.website.idfm.EstimatedCall;
import com.gdamiens.website.idfm.FirstOrLastJourneyEnum;

import java.util.Optional;

public class CallGlobal extends Call {

    private String recordedAtTime;

    private String aimedArrivalTime;

    private String aimedDepartureTime;

    private String arrivalPlatformName;

    private String directionName;

    private String journeyNote;

    private Boolean vehicleAtStop;

    private FirstOrLastJourneyEnum firstOrLastJourney;

    public CallGlobal(EstimatedCall estimatedCall) {
        super(
            estimatedCall.getExpectedArrivalTime(),
            estimatedCall.getExpectedDepartureTime(),
            estimatedCall.getDepartureStatus()
        );
        this.recordedAtTime = estimatedCall.getRecordedAtTime();
        this.aimedArrivalTime = estimatedCall.getAimedArrivalTime();
        this.aimedDepartureTime = estimatedCall.getAimedDepartureTime();
        this.arrivalPlatformName = Optional.ofNullable(estimatedCall.getArrivalPlatformName()).map(ArrivalPlatformName::getValue).orElse(null);
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

    public Boolean getVehicleAtStop() {
        return vehicleAtStop;
    }

    public void setVehicleAtStop(Boolean vehicleAtStop) {
        this.vehicleAtStop = vehicleAtStop;
    }

    public FirstOrLastJourneyEnum getFirstOrLastJourney() {
        return firstOrLastJourney;
    }

    public void setFirstOrLastJourney(FirstOrLastJourneyEnum firstOrLastJourney) {
        this.firstOrLastJourney = firstOrLastJourney;
    }
}
