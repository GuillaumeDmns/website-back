package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.StationsDTO;
import com.gdamiens.website.model.RATPStation;
import com.gdamiens.website.ratp.WsConsumer;
import com.gdamiens.website.ratp.wsdl.GetStationsResponse;
import com.gdamiens.website.ratp.wsdl.WrStations;
import com.gdamiens.website.repository.RATPHistoryRepository;
import com.gdamiens.website.repository.RATPStationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RATPStationService extends RATPService {

    private final RATPStationRepository ratpStationRepository;

    public RATPStationService(WsConsumer wsConsumer,
                              RATPHistoryRepository ratpHistoryRepository,
                              RATPStationRepository ratpStationRepository) {
        super(wsConsumer, ratpHistoryRepository);
        this.ratpStationRepository = ratpStationRepository;
    }

    public StationsDTO getStations(String lineId, String stationName) {
        GetStationsResponse response = wsConsumer.getStations(lineId, stationName);
        WrStations wrStations = response.getReturn();

        this.addToHistory("GetStations", false);

        return new StationsDTO(wrStations.getStations().stream().map(RATPStation::new).collect(Collectors.toList()));
    }

    public List<RATPStation> getStationsNoDTO(String lineId, String stationName) {
        GetStationsResponse response = wsConsumer.getStations(lineId, stationName);
        WrStations wrStations = response.getReturn();

        this.addToHistory("GetStationsNoDTO", false);

        return wrStations.getStations().stream().map(RATPStation::new).collect(Collectors.toList());
    }

    public void deactivateStationsByLine(String lineId) {
        List<RATPStation> stations = this.ratpStationRepository.getAllByLineId(lineId);
        stations.forEach(station -> station.setActive(false));
        this.ratpStationRepository.saveAll(stations);
    }

    public void save(RATPStation ratpStation) {
        this.ratpStationRepository.save(ratpStation);
    }
}
