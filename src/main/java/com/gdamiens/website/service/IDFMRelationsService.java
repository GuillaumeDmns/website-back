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

        // Save stop areas first...

        List<IDFMStopArea> stopAreasList = relations
            .parallelStream()
            .map(relation -> new IDFMStopArea(relation.getStopAreaId()))
            .collect(Collectors.toList());

        idfmStopAreaService.saveStops(stopAreasList);

        // ...then save stops...

        List<IDFMStop> stopsList = relations
            .parallelStream()
            .map(relation -> new IDFMStop(relation.getStopAreaId(), relation.getStopId()))
            .collect(Collectors.toList());

        idfmStopService.saveStops(stopsList);

        // ...and finally save operator stops...

        List<IDFMStopOperator> stopsOperatorList = relations
            .parallelStream()
            .filter(relation -> relation.getStopOperatorId() != null && relation.getStopOperatorVersion() != null)
            .map(relation -> new IDFMStopOperator(relation.getStopId(), relation.getStopOperatorId()))
            .collect(Collectors.toList());

        idfmStopOperatorService.saveStops(stopsOperatorList);

        log.info("Finish importing relations");
    }
}
