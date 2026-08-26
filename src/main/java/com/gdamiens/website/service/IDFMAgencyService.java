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
        log.info("Start truncating agencies table");
        
        long start = System.currentTimeMillis();
        this.idfmAgencyRepository.deleteAllInBatch();
        long end = System.currentTimeMillis();

        log.info("Finish truncating agencies table (took {}ms)", end - start);
    }

    public void saveAllAgenciesFromAgenciesCSVList(List<AgencyCSV> agencyCSVList) {
        if (agencyCSVList == null || agencyCSVList.isEmpty()) {
            return;
        }

        log.info("Start importing agencies");
        log.info("{} agencies to process", agencyCSVList.size());

        List<IDFMAgency> agencies = agencyCSVList.stream()
            .map(IDFMAgency::new)
            .toList();

        this.idfmAgencyRepository.saveAll(agencies);

        log.info("Finish importing agencies");
    }
}
