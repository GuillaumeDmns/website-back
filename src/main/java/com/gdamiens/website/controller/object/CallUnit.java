package com.gdamiens.website.controller.object;

import com.gdamiens.website.idfm.ArrivalPlatformName;
import com.gdamiens.website.idfm.DestinationDisplay;
import com.gdamiens.website.idfm.DestinationName;
import com.gdamiens.website.idfm.DirectionName;
import com.gdamiens.website.idfm.JourneyNote;
import com.gdamiens.website.idfm.LineRef;
import com.gdamiens.website.idfm.MonitoredCall;
import com.gdamiens.website.idfm.MonitoredStopVisit;
import com.gdamiens.website.idfm.MonitoredVehicleJourney;
import com.gdamiens.website.idfm.OperatorRef;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.stream.Collectors;

public class CallUnit extends Call {

    private String id;

    private String destinationDisplay;

    private String arrivalPlatformName;

    private String arrivalStatus;

    private Boolean vehicleAtStop;

    private String lineId;

    private String operatorId;

    private String directionName;

    private String destinationName;

    private String journeyNote;


    public CallUnit(MonitoredStopVisit monitoredStopVisit) {
        super(monitoredStopVisit);

        Optional<MonitoredStopVisit> optionalMonitoredStopVisit = Optional.ofNullable(monitoredStopVisit);

        this.id = optionalMonitoredStopVisit.map(MonitoredStopVisit::getItemIdentifier).orElse(null);

        Optional<MonitoredVehicleJourney> optionalMonitoredVehicleJourney = optionalMonitoredStopVisit
            .map(MonitoredStopVisit::getMonitoredVehicleJourney);
        Optional<MonitoredCall> optionalMonitoredCall = optionalMonitoredVehicleJourney
            .map(MonitoredVehicleJourney::getMonitoredCall);

        if (optionalMonitoredCall.isPresent()) {
            MonitoredCall monitoredCall = optionalMonitoredCall.get();

            this.destinationDisplay = StringUtils.join(
                monitoredCall
                    .getDestinationDisplay()
                    .stream()
                    .map(DestinationDisplay::getValue)
                    .collect(Collectors.toList()),
                ", "
            );
            this.arrivalPlatformName = Optional.ofNullable(monitoredCall.getArrivalPlatformName()).map(ArrivalPlatformName::getValue).orElse(null);
            this.arrivalStatus = monitoredCall.getArrivalStatus();
            this.vehicleAtStop = monitoredCall.getVehicleAtStop();
        }

        if (optionalMonitoredVehicleJourney.isPresent()) {
            MonitoredVehicleJourney monitoredVehicleJourney = optionalMonitoredVehicleJourney.get();

            this.lineId = Optional.ofNullable(monitoredVehicleJourney.getLineRef()).map(LineRef::getValue).orElse(null);
            this.operatorId = Optional.ofNullable(monitoredVehicleJourney.getOperatorRef()).map(OperatorRef::getValue).orElse(null);
            this.directionName = Optional.ofNullable(monitoredVehicleJourney.getDirectionName()).filter(l -> !l.isEmpty()).map(d -> d.get(0)).map(DirectionName::getValue).orElse(null);
            this.destinationName = Optional.ofNullable(monitoredVehicleJourney.getDestinationName()).filter(l -> !l.isEmpty()).map(d -> d.get(0)).map(DestinationName::getValue).orElse(null);
            this.journeyNote = Optional.ofNullable(monitoredVehicleJourney.getJourneyNote()).filter(l -> !l.isEmpty()).map(d -> d.get(0)).map(JourneyNote::getValue).orElse(null);
        }

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDestinationDisplay() {
        return destinationDisplay;
    }

    public void setDestinationDisplay(String destinationDisplay) {
        this.destinationDisplay = destinationDisplay;
    }

    public String getArrivalPlatformName() {
        return arrivalPlatformName;
    }

    public void setArrivalPlatformName(String arrivalPlatformName) {
        this.arrivalPlatformName = arrivalPlatformName;
    }

    public String getArrivalStatus() {
        return arrivalStatus;
    }

    public void setArrivalStatus(String arrivalStatus) {
        this.arrivalStatus = arrivalStatus;
    }

    public Boolean getVehicleAtStop() {
        return vehicleAtStop;
    }

    public void setVehicleAtStop(Boolean vehicleAtStop) {
        this.vehicleAtStop = vehicleAtStop;
    }

    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getDirectionName() {
        return directionName;
    }

    public void setDirectionName(String directionName) {
        this.directionName = directionName;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getJourneyNote() {
        return journeyNote;
    }

    public void setJourneyNote(String journeyNote) {
        this.journeyNote = journeyNote;
    }
}
