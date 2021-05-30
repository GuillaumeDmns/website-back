package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.LineRequest;
import com.gdamiens.website.controller.object.LinesDTO;
import com.gdamiens.website.controller.object.NextMissionsDTO;
import com.gdamiens.website.controller.object.StationsDTO;
import com.gdamiens.website.model.RATPHistory;
import com.gdamiens.website.ratp.WsConsumer;
import com.gdamiens.website.ratp.wsdl.*;
import com.gdamiens.website.repository.RATPHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public abstract class RATPService {
    protected final WsConsumer wsConsumer;

    protected final RATPHistoryRepository ratpHistoryRepository;

    public RATPService(WsConsumer wsConsumer, RATPHistoryRepository ratpHistoryRepository) {
        this.wsConsumer = wsConsumer;
        this.ratpHistoryRepository = ratpHistoryRepository;
    }

    protected void addToHistory(String description) {
        RATPHistory ratpHistory = new RATPHistory(description, 1);

        this.ratpHistoryRepository.save(ratpHistory);
    }

}
