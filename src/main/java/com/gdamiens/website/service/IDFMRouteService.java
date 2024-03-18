package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.controller.object.gtfs.AgencyCSV;
import com.gdamiens.website.controller.object.gtfs.RoutesCSV;
import com.gdamiens.website.model.IDFMAgency;
import com.gdamiens.website.model.IDFMRoute;
import com.gdamiens.website.repository.IDFMAgencyRepository;
import com.gdamiens.website.repository.IDFMRouteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class IDFMRouteService extends AbstractIDFMService implements IDFMServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(IDFMRouteService.class);

    private final IDFMRouteRepository idfmRouteRepository;

    public IDFMRouteService(IDFMRouteRepository idfmRouteRepository, ApplicationProperties applicationProperties) {
        super(applicationProperties);
        this.idfmRouteRepository = idfmRouteRepository;
    }

    public void truncateTable() {
        log.info("Start deleting all routes");
        this.idfmRouteRepository.deleteAllInBatch();
        log.info("Finish deleting all routes");
    }

    public void saveAllRoutesFromRoutesCSVList(List<RoutesCSV> routesCSVList) {
        log.info("Start importing routes");
        log.info("{} routes to process", routesCSVList.size());

        this.idfmRouteRepository.saveAll(
            routesCSVList.parallelStream().map(IDFMRoute::new).collect(Collectors.toList())
        );

        log.info("Finish importing routes");
    }
}
