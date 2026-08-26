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
        log.info("Start truncating operators table");
        
        long start = System.currentTimeMillis();
        this.idfmOperatorRepository.deleteAllInBatch();
        long end = System.currentTimeMillis();

        log.info("Finish truncating operators table (took {}ms)", end - start);
    }

    public void saveAllOperatorsFromCSV() {
        CSVReader<OperatorsCSV> csvReader = new CSVReader<>(OperatorsCSV.class);

        List<OperatorsCSV> operators = csvReader.readFromUrl(Constants.IDFM_OPERATORS_URL);

        if (operators == null || operators.isEmpty()) {
            log.info("No data has been found in the operators CSV file");
            return;
        }

        log.info("Start importing operators");
        log.info("{} operators to process", operators.size());

        this.idfmOperatorRepository.saveAll(
            operators.stream()
                .map(IDFMOperator::new)
                .toList()
        );

        log.info("Finish importing operators");
    }
}
