package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.LineRequest;
import com.gdamiens.website.controller.object.StationsDTO;
import com.gdamiens.website.ratp.WsConsumer;
import com.gdamiens.website.ratp.wsdl.GetStationsResponse;
import com.gdamiens.website.ratp.wsdl.WrStations;
import com.gdamiens.website.repository.RATPHistoryRepository;
import org.springframework.stereotype.Service;

@Service
public class RATPStationService extends RATPService {

    public RATPStationService(WsConsumer wsConsumer, RATPHistoryRepository ratpHistoryRepository) {
        super(wsConsumer, ratpHistoryRepository);
    }

    public StationsDTO getStations(String id, String name, String sens, LineRequest line, Integer limit, Boolean isSortedAlpha) {
        GetStationsResponse response = wsConsumer.getStations(id, name, sens, line, limit, isSortedAlpha);
        WrStations wrStations = response.getReturn();

        this.addToHistory("GetStations", false);

        return new StationsDTO(wrStations);
    }
}
