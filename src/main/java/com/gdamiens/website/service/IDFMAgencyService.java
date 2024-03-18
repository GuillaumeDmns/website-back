package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.controller.object.OperatorsCSV;
import com.gdamiens.website.controller.object.gtfs.AgencyCSV;
import com.gdamiens.website.model.IDFMAgency;
import com.gdamiens.website.model.IDFMOperator;
import com.gdamiens.website.repository.IDFMAgencyRepository;
import com.gdamiens.website.repository.IDFMOperatorRepository;
import com.gdamiens.website.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class IDFMAgencyService extends AbstractIDFMService implements IDFMServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(IDFMAgencyService.class);

    private final IDFMAgencyRepository idfmAgencyRepository;

    public IDFMAgencyService(IDFMAgencyRepository idfmAgencyRepository, ApplicationProperties applicationProperties) {
        super(applicationProperties);
        this.idfmAgencyRepository = idfmAgencyRepository;
    }

    public void truncateTable() {
        log.info("Start deleting all agencies");
        this.idfmAgencyRepository.deleteAllInBatch();
        log.info("Finish deleting all agencies");
    }

    public void saveAllAgenciesFromAgenciesCSVList(List<AgencyCSV> agencyCSVList) {
        log.info("Start importing agencies");
        log.info("{} agencies to process", agencyCSVList.size());

        this.idfmAgencyRepository.saveAll(
            agencyCSVList.parallelStream().map(IDFMAgency::new).collect(Collectors.toList())
        );

        log.info("Finish importing agencies");
    }
}
