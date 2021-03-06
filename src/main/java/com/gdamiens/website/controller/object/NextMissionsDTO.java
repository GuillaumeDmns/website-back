package com.gdamiens.website.controller.object;

import com.gdamiens.website.ratp.wsdl.WrMissions;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class NextMissionsDTO extends RATPResponse {

    private String ambiguityMessage;

    private String requestedDate;

    private List<MissionCustom> nextMissions;

    public NextMissionsDTO(WrMissions wrMissions) {
        super();
        this.ambiguityMessage = wrMissions.getAmbiguityMessage();
        this.requestedDate = wrMissions.getArgumentDate();

        if (wrMissions.getAmbiguityMessage() == null && wrMissions.getMissions() != null) {
            this.nextMissions = wrMissions
                    .getMissions()
                    .stream()
                    .map(MissionCustom::new)
                    .sorted(Comparator.comparing(MissionCustom::getNextPassage))
                    .collect(Collectors.toList());
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

    public List<MissionCustom> getNextMissions() {
        return nextMissions;
    }

    public void setNextMissions(List<MissionCustom> nextMissions) {
        this.nextMissions = nextMissions;
    }
}
