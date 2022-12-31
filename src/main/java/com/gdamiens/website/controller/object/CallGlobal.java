package com.gdamiens.website.controller.object;

import com.gdamiens.website.idfm.ArrivalPlatformName;
import com.gdamiens.website.idfm.DestinationDisplay;
import com.gdamiens.website.idfm.EstimatedCall;
import com.gdamiens.website.idfm.FirstOrLastJourneyEnum;

import java.util.Optional;

public class CallGlobal extends Call {

    private String recordedAtTime;

    private String arrivalPlatformName;

    private String directionName;

    private String journeyNote;

    private FirstOrLastJourneyEnum firstOrLastJourney;

    public CallGlobal(EstimatedCall estimatedCall) {
        super(
            estimatedCall.getExpectedDepartureTime(),
            estimatedCall.getExpectedArrivalTime(),
            estimatedCall.getAimedDepartureTime(),
            estimatedCall.getAimedArrivalTime(),
            estimatedCall.getDepartureStatus()
        );
        this.recordedAtTime = estimatedCall.getRecordedAtTime();
        this.arrivalPlatformName = Optional
            .ofNullable(estimatedCall.getArrivalPlatformName())
            .map(ArrivalPlatformName::getValue)
            .orElse(null);
        this.directionName = Optional
            .ofNullable(estimatedCall.getDestinationDisplay())
            .filter(l -> !l.isEmpty())
            .map(a -> a.get(0))
            .map(DestinationDisplay::getValue)
            .orElse(null);
        this.journeyNote = estimatedCall.getJourneyNote();
        this.firstOrLastJourney = estimatedCall.getFirstOrLastJourney();
    }

    public String getRecordedAtTime() {
        return recordedAtTime;
    }

    public void setRecordedAtTime(String recordedAtTime) {
        this.recordedAtTime = recordedAtTime;
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

    public FirstOrLastJourneyEnum getFirstOrLastJourney() {
        return firstOrLastJourney;
    }

    public void setFirstOrLastJourney(FirstOrLastJourneyEnum firstOrLastJourney) {
        this.firstOrLastJourney = firstOrLastJourney;
    }
}
