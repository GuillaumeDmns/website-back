package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.FullMissionDTO;
import com.gdamiens.website.controller.object.NextMissionsDTO;
import com.gdamiens.website.controller.object.StationsDTO;
import com.gdamiens.website.ratp.WsConsumer;
import com.gdamiens.website.ratp.wsdl.GetMissionsNextResponse;
import com.gdamiens.website.ratp.wsdl.Station;
import com.gdamiens.website.ratp.wsdl.WrMissions;
import com.gdamiens.website.repository.RATPHistoryRepository;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RATPMissionService extends RATPService {

    private final RATPStationService ratpStationService;

    public RATPMissionService(WsConsumer wsConsumer, RATPHistoryRepository ratpHistoryRepository, RATPStationService ratpStationService) {
        super(wsConsumer, ratpHistoryRepository);
        this.ratpStationService = ratpStationService;
    }

    public NextMissionsDTO getNext(String lineId, String stationId) {
        GetMissionsNextResponse response = wsConsumer.getMissionsNext(lineId, stationId, 6);
        WrMissions wrMissions = response.getReturn();

        this.addToHistory("getNext", false);

        return new NextMissionsDTO(wrMissions);
    }

    public FullMissionDTO getFullMission(String lineId) {

        StationsDTO stationsDTO = this.ratpStationService.getStations(lineId, "*");

        List<Pair<String, String>> stationConnexions = new ArrayList<>();

        stationsDTO.getStations().forEach(station -> {
            GetMissionsNextResponse response = wsConsumer.getMissionsNext(lineId, station.getId(), 6);
            WrMissions wrMissions = response.getReturn();

            this.addToHistory("getFullMission", false);

            Station argumentStation = wrMissions.getArgumentStation();

            if (argumentStation != null && !argumentStation.getId().isEmpty()) {
                List<String> stationsA = argumentStation.getIdsNextA();
                if (stationsA != null && stationsA.size() > 0 && stationsA.get(0) != null && !stationsA.get(0).isBlank()) {
                    stationConnexions.add(Pair.of(wrMissions.getArgumentStation().getId(), wrMissions.getArgumentStation().getIdsNextA().get(0)));
                }
                List<String> stationsR = argumentStation.getIdsNextR();
                if (stationsR != null && stationsR.size() > 0 && stationsR.get(0) != null && !stationsR.get(0).isBlank()) {
                    stationConnexions.add(Pair.of(wrMissions.getArgumentStation().getId(), wrMissions.getArgumentStation().getIdsNextR().get(0)));
                }
            }
        });

        return new FullMissionDTO(stationsDTO, stationConnexions);
    }
}
