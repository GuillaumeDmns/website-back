package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.RelationsCSV;
import com.gdamiens.website.model.IDFMStop;
import com.gdamiens.website.model.IDFMStopArea;
import com.gdamiens.website.model.IDFMStopOperator;
import com.gdamiens.website.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class IDFMRelationsService {

    private static final Logger log = LoggerFactory.getLogger(IDFMRelationsService.class);

    private final IDFMStopService idfmStopService;

    private final IDFMStopAreaService idfmStopAreaService;

    private final IDFMStopOperatorService idfmStopOperatorService;

    public IDFMRelationsService(IDFMStopService idfmStopService,
                                IDFMStopAreaService idfmStopAreaService,
                                IDFMStopOperatorService idfmStopOperatorService) {
        this.idfmStopService = idfmStopService;
        this.idfmStopAreaService = idfmStopAreaService;
        this.idfmStopOperatorService = idfmStopOperatorService;
    }

    public void saveRelationsFromCSV() {
        CSVReader<RelationsCSV> csvReader = new CSVReader<>(RelationsCSV.class);

        List<RelationsCSV> relations = csvReader.readFromUrl(Constants.IDFM_RELATIONS_URL);

        log.info("Start importing relations between stops");
        log.info("{} relations to process", relations.size());

        Map<Integer, Map<Integer, Integer>> relationsMap = relations
            .stream()
            .filter(relationsCSV -> relationsCSV.getStopId() != null)
            .collect(
                Collectors.groupingBy(
                    RelationsCSV::getStopAreaId,
                    Collectors.groupingBy(
                        RelationsCSV::getStopId,
                        Collectors.collectingAndThen(Collectors.toList(), values -> values.get(0).getStopOperatorId()))
                )
            );

        // Save stop areas first...

        List<IDFMStopArea> stopAreasList = relationsMap
            .keySet()
            .stream()
            .map(IDFMStopArea::new)
            .collect(Collectors.toList());

        idfmStopAreaService.saveStops(stopAreasList);

        // ...then save stops...

        List<IDFMStop> stopsList = relationsMap
            .entrySet()
            .stream()
            .map(entry -> new IDFMStop(entry.getKey(), entry.getValue().get(0)))
            .collect(Collectors.toList());

        idfmStopService.saveStops(stopsList);

        // ...and finally save operator stops...

        List<IDFMStopOperator> stopsOperatorList = relationsMap
            .values()
            .stream()
            .flatMap(entry -> entry.entrySet().stream())
            .map(entry -> new IDFMStopOperator(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());

        idfmStopOperatorService.saveStops(stopsOperatorList);

        log.info("Finish importing relations");
    }
}
