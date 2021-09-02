package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.FullMissionDTO;
import com.gdamiens.website.controller.object.NextMissionsDTO;
import com.gdamiens.website.controller.object.StationsDTO;
import com.gdamiens.website.ratp.WsConsumer;
import com.gdamiens.website.ratp.wsdl.GetMissionsNextResponse;
import com.gdamiens.website.ratp.wsdl.Mission;
import com.gdamiens.website.ratp.wsdl.Station;
import com.gdamiens.website.ratp.wsdl.WrMissions;
import com.gdamiens.website.repository.RATPHistoryRepository;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        List<Pair<String, String>> stationConnexionsA = new ArrayList<>();
        List<Pair<String, String>> stationConnexionsR = new ArrayList<>();
        List<Triple<String, String, Long>> stationConnexions = new ArrayList<>();
        Map<String, Pair<List<String>, List<String>>> nextMissionsByStation = new HashMap<>();

        stationsDTO.getStations().parallelStream().forEach(station -> {
            GetMissionsNextResponse response = wsConsumer.getMissionsNext(lineId, station.getId(), 6);
            WrMissions wrMissions = response.getReturn();

            this.addToHistory("getFullMission", false);

            Station argumentStation = wrMissions.getArgumentStation();

            if (argumentStation != null && !argumentStation.getId().isEmpty()) {
                List<String> stationsA = argumentStation.getIdsNextA();
                if (stationsA != null && !stationsA.isEmpty()) {
                    stationsA.forEach(stationA -> {
                        if (stationA != null && !stationA.isBlank()) {
                            stationConnexionsA.add(Pair.of(argumentStation.getId(), stationA));
                        }
                    });
                }
                List<String> stationsR = argumentStation.getIdsNextR();
                if (stationsR != null && !stationsR.isEmpty()) {
                    stationsR.forEach(stationR -> {
                        if (stationR != null && !stationR.isBlank()) {
                            stationConnexionsR.add(Pair.of(argumentStation.getId(), stationR));
                        }
                    });
                }
            }

            List<Mission> missions = wrMissions.getMissions();

            List<String> a = new ArrayList<>();
            List<String> r = new ArrayList<>();

            missions.forEach(mission -> {
                if (mission != null && mission.getDirection() != null && mission.getStationsDates() != null && !mission.getStationsDates().isEmpty()) {
                    if ("A".equals(mission.getDirection().getSens())) {
                        a.add(mission.getStationsDates().get(0));
                    }
                    if ("R".equals(mission.getDirection().getSens())) {
                        r.add(mission.getStationsDates().get(0));
                    }
                }
            });

            nextMissionsByStation.put(station.getId(), Pair.of(a, r));
        });


        computeStationConnexionCount(stationConnexionsA, stationConnexions, nextMissionsByStation, true);
        computeStationConnexionCount(stationConnexionsR, stationConnexions, nextMissionsByStation, false);

        return new FullMissionDTO(stationsDTO, stationConnexions);
    }

    private void computeStationConnexionCount(List<Pair<String, String>> stationConnexionsOld,
                                              List<Triple<String, String, Long>> stationConnexionsNew,
                                              Map<String, Pair<List<String>, List<String>>> nextMissionsByStation,
                                              boolean isA) {

        stationConnexionsOld.forEach(stationConnexion -> {
            String idSource = stationConnexion.getFirst();
            String idDestination = stationConnexion.getSecond();

            List<String> missionsSource = isA ? nextMissionsByStation.get(idSource).getFirst() : nextMissionsByStation.get(idSource).getSecond();
            List<String> missionsDestination = isA ? nextMissionsByStation.get(idDestination).getFirst() : nextMissionsByStation.get(idDestination).getSecond();

            long count = 0L;
            if (!missionsSource.isEmpty()) {
                count = missionsDestination
                        .stream()
                        .filter(missionDestination -> missionDestination.compareTo(missionsSource.get(0)) <= 0)
                        .count();
            }
            stationConnexionsNew.add(Triple.of(stationConnexion.getFirst(), stationConnexion.getSecond(), count));
        });
    }
}
