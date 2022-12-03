package com.gdamiens.website.controller.object;

import com.gdamiens.website.idfm.DepartureStatusEnum;
import com.gdamiens.website.idfm.EstimatedCall;
import com.gdamiens.website.model.IDFMStop;

public class Call {

    private String expectedArrivalTime;

    private String expectedDepartureTime;

    private DepartureStatusEnum departureStatus;

    private String directionName;

    private IDFMStop stop;

    public Call(EstimatedCall estimatedCall, IDFMStop stop) {
        this.expectedArrivalTime = estimatedCall.getExpectedArrivalTime();
        this.expectedDepartureTime = estimatedCall.getExpectedDepartureTime();
        this.departureStatus = estimatedCall.getDepartureStatus();
        this.directionName = estimatedCall.getDestinationDisplay().get(0).getValue();
        this.stop = stop;
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

    public String getDirectionName() {
        return directionName;
    }

    public void setDirectionName(String directionName) {
        this.directionName = directionName;
    }

    public IDFMStop getStop() {
        return stop;
    }

    public void setStop(IDFMStop stop) {
        this.stop = stop;
    }
}
