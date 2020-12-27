package com.gdamiens.website.service;

import com.gdamiens.website.ratp.WsConsumer;
import com.gdamiens.website.ratp.wsdl.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RATPService {
    private final WsConsumer wsConsumer;

    public RATPService(WsConsumer wsConsumer) {
        this.wsConsumer = wsConsumer;
    }

    public List<Line> getRerAInfos() {
        GetLinesResponse response = wsConsumer.getLines("RA");
        return response.getReturn();
    }

    public List<Mission> getNext(String lineId, String stationName) {
        GetMissionsNextResponse response = wsConsumer.getMissionsNext(lineId, stationName);

        WrMissions wrMissions = response.getReturn();
        return wrMissions.getMissions();
    }
}
