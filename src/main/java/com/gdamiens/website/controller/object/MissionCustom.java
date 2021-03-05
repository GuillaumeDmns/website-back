package com.gdamiens.website.controller.object;

import com.gdamiens.website.ratp.wsdl.Mission;

import java.util.List;

public class MissionCustom {

    private String code;

    private String direction;

    private String nextPassage;

    private List<String> messages;

    public MissionCustom(Mission mission) {
        this.code = mission.getCode();
        if (mission.getDirection() != null) {
            this.direction = mission.getDirection().getName();
        }

        if (mission.getStationsDates() != null && mission.getStationsDates().size() > 0) {
            this.nextPassage = mission.getStationsDates().get(0);
        }

        if (mission.getStationsMessages() != null) {
            this.messages = mission.getStationsMessages();
        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getNextPassage() {
        return nextPassage;
    }

    public void setNextPassage(String nextPassage) {
        this.nextPassage = nextPassage;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}
