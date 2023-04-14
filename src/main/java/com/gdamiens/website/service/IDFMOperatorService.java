package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.controller.object.OperatorsCSV;
import com.gdamiens.website.model.IDFMOperator;
import com.gdamiens.website.repository.IDFMOperatorRepository;
import com.gdamiens.website.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class IDFMOperatorService extends AbstractIDFMService implements IDFMServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(IDFMOperatorService.class);

    private final IDFMOperatorRepository idfmOperatorRepository;

    public IDFMOperatorService(IDFMOperatorRepository idfmOperatorRepository, ApplicationProperties applicationProperties) {
        super(applicationProperties);
        this.idfmOperatorRepository = idfmOperatorRepository;
    }

    public void truncateTable() {
        log.info("Start deleting all operators");
        this.idfmOperatorRepository.deleteAllInBatch();
        log.info("Finish deleting all operators");
    }

    public void saveAllOperatorsFromCSV() {
        CSVReader<OperatorsCSV> csvReader = new CSVReader<>(OperatorsCSV.class);

        List<OperatorsCSV> operators = csvReader.readFromUrl(Constants.IDFM_OPERATORS_URL);

        log.info("Start importing operators");
        log.info("{} operators to process", operators.size());

        this.idfmOperatorRepository.saveAll(
            operators.parallelStream().map(IDFMOperator::new).collect(Collectors.toList())
        );

        log.info("Finish importing operators");
    }
}
