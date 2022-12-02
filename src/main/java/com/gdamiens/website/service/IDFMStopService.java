package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.StationCSV;
import com.gdamiens.website.model.IDFMStop;
import com.gdamiens.website.repository.IDFMStopRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class IDFMStopService {

    private static final Logger log = LoggerFactory.getLogger(IDFMStopService.class);

    private final IDFMStopRepository idfmStopRepository;

    public IDFMStopService(IDFMStopRepository idfmStopRepository) {
        this.idfmStopRepository = idfmStopRepository;
    }

    public void saveAllStopFromId(List<Integer> stops) {
        this.idfmStopRepository.saveAll(stops.stream().map(IDFMStop::new).collect(Collectors.toList()));
    }

    public void saveAllStopsFromCSV(List<StationCSV> stops) {
        this.idfmStopRepository.saveAll(stops.stream().map(IDFMStop::new).collect(Collectors.toList()));
    }
}
