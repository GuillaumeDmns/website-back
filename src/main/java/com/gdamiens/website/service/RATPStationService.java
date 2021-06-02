package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.StationsDTO;
import com.gdamiens.website.model.RATPStation;
import com.gdamiens.website.ratp.WsConsumer;
import com.gdamiens.website.ratp.wsdl.GetStationsResponse;
import com.gdamiens.website.ratp.wsdl.WrStations;
import com.gdamiens.website.repository.RATPHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class RATPStationService extends RATPService {

    public RATPStationService(WsConsumer wsConsumer, RATPHistoryRepository ratpHistoryRepository) {
        super(wsConsumer, ratpHistoryRepository);
    }

    public StationsDTO getStations(String lineId, String stationName) {
        GetStationsResponse response = wsConsumer.getStations(lineId, stationName);
        WrStations wrStations = response.getReturn();

        this.addToHistory("GetStations", false);

        return new StationsDTO(wrStations.getStations().stream().map(RATPStation::new).collect(Collectors.toList()));
    }


}
