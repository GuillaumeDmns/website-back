package com.gdamiens.website.service;

import com.gdamiens.website.model.IDFMStopLine;
import com.gdamiens.website.repository.IDFMStopLineRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IDFMStopLineService {

    private final IDFMStopLineRepository stopLineRepository;

    public IDFMStopLineService(IDFMStopLineRepository stopLineRepository) {
        this.stopLineRepository = stopLineRepository;
    }

    public void saveAllStopLine(List<IDFMStopLine> stopline) {
        this.stopLineRepository.saveAll(stopline);
    }
}
