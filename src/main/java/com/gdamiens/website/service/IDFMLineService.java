package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.LineCSV;
import com.gdamiens.website.controller.object.StationAndLineCSV;
import com.gdamiens.website.model.IDFMLine;
import com.gdamiens.website.model.IDFMStopLine;
import com.gdamiens.website.repository.IDFMLineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IDFMLineService {

    private static final Logger log = LoggerFactory.getLogger(IDFMLineService.class);

    private final IDFMStopService idfmStopService;

    private final IDFMStopLineService idfmStopLineService;

    private final IDFMLineRepository idfmLineRepository;

    public IDFMLineService(IDFMStopService idfmStopService, IDFMStopLineService idfmStopLineService, IDFMLineRepository idfmLineRepository) {
        this.idfmStopService = idfmStopService;
        this.idfmStopLineService = idfmStopLineService;
        this.idfmLineRepository = idfmLineRepository;
    }

    public void refreshLinesAndStops(Map<String, List<StationAndLineCSV>> linesAndStops) {
        // Update list of lines
        log.info("Start importing list of lines");
        this.idfmLineRepository.saveAll(
            linesAndStops
            .keySet()
            .parallelStream()
            .filter(line -> "STIF".equals(line.split(":")[0]))
            .map(line -> new IDFMLine(line.split(":")[3]))
            .collect(Collectors.toList())
        );
        log.info("Finished importing list of lines");

        log.info("Start importing list of stops");
        this.idfmStopService.saveAllStopFromId(
            linesAndStops
                .values()
                .parallelStream()
                .flatMap(List::stream)
                .filter(stop -> "STIF".equals(stop.getMonitoringRefZDE().split(":")[0]))
                .map(stop -> Integer.parseInt(stop.getMonitoringRefZDE().split(":")[3]))
                .distinct()
                .collect(Collectors.toList())
        );
        log.info("Finished importing list of stops");

        log.info("Start importing stop/line pairs");
        linesAndStops
            .entrySet()
            .parallelStream()
            .filter(lineAndStop -> "STIF".equals(lineAndStop.getKey().split(":")[0]))
            .forEach(lineAndStop -> {
                String lineId = lineAndStop.getKey().split(":")[3];

                this.idfmStopLineService.saveAllStopLine(
                    lineAndStop
                        .getValue()
                        .stream()
                        .filter(stop -> "STIF".equals(stop.getMonitoringRefZDE().split(":")[0]))
                        .map(stop -> new IDFMStopLine(lineId, Integer.parseInt(stop.getMonitoringRefZDE().split(":")[3])))
                        .collect(Collectors.toList())
                );

        });
        log.info("Finished importing stop/line pairs");
    }

    public void saveAllLinesFromCSV(List<LineCSV> stops) {
        this.idfmLineRepository.saveAll(stops.stream().map(IDFMLine::new).collect(Collectors.toList()));
    }
}
