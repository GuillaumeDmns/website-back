package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.model.IDFMStop;
import com.gdamiens.website.model.Test;
import com.gdamiens.website.repository.IDFMStopRepository;
import com.gdamiens.website.repository.TestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class IDFMStopService extends AbstractIDFMService implements IDFMServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(IDFMStopService.class);

    private final IDFMStopRepository idfmStopRepository;

    private final TestRepository testRepository;


    public IDFMStopService(IDFMStopRepository idfmStopRepository, TestRepository testRepository, ApplicationProperties applicationProperties) {
        super(applicationProperties);
        this.idfmStopRepository = idfmStopRepository;
        this.testRepository = testRepository;
    }

    @Override
    public void truncateTable() {
        log.info("Start deleting all stops");
        this.idfmStopRepository.deleteAllInBatch();
        log.info("Finish deleting all stops");
    }

    public IDFMStop getStop(Integer stopId) {
        return this.idfmStopRepository.findById(stopId).orElse(null);
    }

    public Test createOrUpdate(Test test) {
        return this.testRepository.save(test);
    }

    public Test get(Integer id) {
        return this.testRepository.findById(id).orElse(null);
    }
}
