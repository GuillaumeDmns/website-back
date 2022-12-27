package com.gdamiens.website.service;

import com.gdamiens.website.configuration.ApplicationProperties;
import com.gdamiens.website.controller.object.OperatorsCSV;
import com.gdamiens.website.model.IDFMOperator;
import com.gdamiens.website.repository.IDFMOperatorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class IDFMOperatorService extends AbstractIDFMService {

    private static final Logger log = LoggerFactory.getLogger(IDFMOperatorService.class);

    private final IDFMOperatorRepository idfmOperatorRepository;

    public IDFMOperatorService(IDFMOperatorRepository idfmOperatorRepository, ApplicationProperties applicationProperties) {
        super(applicationProperties);
        this.idfmOperatorRepository = idfmOperatorRepository;
    }

    public void saveAllOperatorsFromCSV(List<OperatorsCSV> operators) {
        this.idfmOperatorRepository.saveAll(
            operators.parallelStream().map(IDFMOperator::new).collect(Collectors.toList())
        );
    }
}
