package com.gdamiens.website.controller.object;

import com.gdamiens.website.idfm.MonitoredCall;

public class CallUnit extends Call {

    private Boolean vehicleAtStop;

    public CallUnit(MonitoredCall monitoredCall) {
        super(
            monitoredCall.getExpectedArrivalTime(),
            monitoredCall.getExpectedArrivalTime(),
            monitoredCall.getDepartureStatus());
        this.vehicleAtStop = monitoredCall.getVehicleAtStop();
    }

    public Boolean getVehicleAtStop() {
        return vehicleAtStop;
    }

    public void setVehicleAtStop(Boolean vehicleAtStop) {
        this.vehicleAtStop = vehicleAtStop;
    }
}
