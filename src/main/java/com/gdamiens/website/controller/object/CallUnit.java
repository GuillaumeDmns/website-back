package com.gdamiens.website.controller.object;

import com.gdamiens.website.idfm.ArrivalPlatformName;
import com.gdamiens.website.idfm.DestinationDisplay;
import com.gdamiens.website.idfm.MonitoredCall;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.stream.Collectors;

public class CallUnit extends Call {

    private String destinationDisplay;

    private String arrivalPlatformName;

    private String arrivalStatus;

    private Boolean vehicleAtStop;

    public CallUnit(MonitoredCall monitoredCall) {
        super(
            monitoredCall.getExpectedDepartureTime(),
            monitoredCall.getExpectedArrivalTime(),
            monitoredCall.getAimedDepartureTime(),
            monitoredCall.getAimedArrivalTime(),
            monitoredCall.getDepartureStatus()
        );
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
}
