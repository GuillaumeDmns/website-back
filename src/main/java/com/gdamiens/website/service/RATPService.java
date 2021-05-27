package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.LineRequest;
import com.gdamiens.website.controller.object.LinesDTO;
import com.gdamiens.website.controller.object.NextMissionsDTO;
import com.gdamiens.website.controller.object.StationsDTO;
import com.gdamiens.website.model.RATPHistory;
import com.gdamiens.website.ratp.WsConsumer;
import com.gdamiens.website.ratp.wsdl.*;
import com.gdamiens.website.repository.RATPHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class RATPService {
    private final WsConsumer wsConsumer;

    private final RATPHistoryRepository ratpHistoryRepository;

    public RATPService(WsConsumer wsConsumer, RATPHistoryRepository ratpHistoryRepository) {
        this.wsConsumer = wsConsumer;
        this.ratpHistoryRepository = ratpHistoryRepository;
    }

    private void addToHistory(String description) {
        RATPHistory ratpHistory = new RATPHistory(description, 1);

        this.ratpHistoryRepository.save(ratpHistory);
    }

    public StationsDTO getStations(String id, String name, String sens, LineRequest line, Integer limit, Boolean isSortedAlpha) {
        GetStationsResponse response = wsConsumer.getStations(id, name, sens, line, limit, isSortedAlpha);
        WrStations wrStations = response.getReturn();

        addToHistory("GetStations");

        return new StationsDTO(wrStations);
    }

    public LinesDTO getLinesInfos(String lineId, String code, String codeStif, String realm, String codeReseau) {
        GetLinesResponse response = wsConsumer.getLines(lineId, code, codeStif, realm, codeReseau);

        if (response != null && response.getReturn().size() > 0 && response.getReturn().get(0) != null) {
            return new LinesDTO(response.getReturn());
        }

        addToHistory("GetLinesInfos");

        return new LinesDTO(new ArrayList<>());
    }

    public NextMissionsDTO getNext(String lineId, String stationName) {
        GetMissionsNextResponse response = wsConsumer.getMissionsNext(lineId, stationName);
        WrMissions wrMissions = response.getReturn();

        addToHistory("getNext");

        return new NextMissionsDTO(wrMissions);
    }
}
