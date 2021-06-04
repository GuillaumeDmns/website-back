package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.NextMissionsDTO;
import com.gdamiens.website.ratp.WsConsumer;
import com.gdamiens.website.ratp.wsdl.GetMissionsNextResponse;
import com.gdamiens.website.ratp.wsdl.WrMissions;
import com.gdamiens.website.repository.RATPHistoryRepository;
import org.springframework.stereotype.Service;

@Service
public class RATPMissionService extends RATPService {

    public RATPMissionService(WsConsumer wsConsumer, RATPHistoryRepository ratpHistoryRepository) {
        super(wsConsumer, ratpHistoryRepository);
    }

    public NextMissionsDTO getNext(String lineId, String stationId) {
        GetMissionsNextResponse response = wsConsumer.getMissionsNext(lineId, stationId, 6);
        WrMissions wrMissions = response.getReturn();

        this.addToHistory("getNext", false);

        return new NextMissionsDTO(wrMissions);
    }
}
