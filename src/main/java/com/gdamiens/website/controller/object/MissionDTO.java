package com.gdamiens.website.controller.object;

import com.gdamiens.website.ratp.wsdl.WrMission;
import com.gdamiens.website.ratp.wsdl.Mission;

public class MissionDTO extends RATPResponse {

    private String ambiguityMessage;

    private String requestedDate;

    private Mission nextMission;

    public MissionDTO(WrMission wrMission) {
        super();
        this.ambiguityMessage = wrMission.getAmbiguityMessage();
        this.requestedDate = wrMission.getArgumentDate();

        if (wrMission.getAmbiguityMessage() == null && wrMission.getMission() != null) {
//            this.nextMission = new MissionCustom(wrMission.getMission());
            this.nextMission = wrMission.getMission();
        }
    }

    public String getAmbiguityMessage() {
        return ambiguityMessage;
    }

    public void setAmbiguityMessage(String ambiguityMessage) {
        this.ambiguityMessage = ambiguityMessage;
    }

    public String getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(String requestedDate) {
        this.requestedDate = requestedDate;
    }

    public Mission getNextMission() {
        return nextMission;
    }

    public void setNextMission(Mission nextMission) {
        this.nextMission = nextMission;
    }

}
