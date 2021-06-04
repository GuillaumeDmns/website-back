package com.gdamiens.website.controller.object;

import com.gdamiens.website.ratp.wsdl.Mission;

public class MissionCustom {

    private String id;

    private String code;

    private String direction;

    private String stationEndLine;

    private String nextPassage;

    private String messages;

    private Boolean isStationStop;

    private String platform;

    public MissionCustom(Mission mission) {
        if (mission != null) {
            this.id = mission.getId();
            this.code = mission.getCode();
            if (mission.getDirection() != null) {
                this.direction = mission.getDirection().getName();
            }

            if (mission.getStationsDates() != null && mission.getStationsDates().size() > 0) {
                this.nextPassage = mission.getStationsDates().get(0);
            }

            if (mission.getStationsMessages() != null && mission.getStationsMessages().size() > 0) {
                this.messages = mission.getStationsMessages().get(0);
            }

            if (mission.getStationsStops() != null && mission.getStationsStops().size() > 0) {
                this.isStationStop = mission.getStationsStops().get(0);
            }

            if (mission.getStationsPlatforms() != null && mission.getStationsPlatforms().size() > 0) {
                this.platform = mission.getStationsPlatforms().get(0);
            }

            if (mission.getStationEndLine() != null) {
                this.stationEndLine = mission.getStationEndLine().getName();
            }
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getStationEndLine() {
        return stationEndLine;
    }

    public void setStationEndLine(String stationEndLine) {
        this.stationEndLine = stationEndLine;
    }

    public String getNextPassage() {
        return nextPassage;
    }

    public void setNextPassage(String nextPassage) {
        this.nextPassage = nextPassage;
    }

    public String getMessages() {
        return messages;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }

    public Boolean getStationStop() {
        return isStationStop;
    }

    public void setStationStop(Boolean stationStop) {
        isStationStop = stationStop;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
