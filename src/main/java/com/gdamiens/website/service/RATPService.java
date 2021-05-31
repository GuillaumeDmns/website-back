package com.gdamiens.website.service;

import com.gdamiens.website.model.RATPHistory;
import com.gdamiens.website.ratp.WsConsumer;
import com.gdamiens.website.repository.RATPHistoryRepository;
import org.springframework.stereotype.Service;


@Service
public abstract class RATPService {
    protected final WsConsumer wsConsumer;

    protected final RATPHistoryRepository ratpHistoryRepository;

    public RATPService(WsConsumer wsConsumer, RATPHistoryRepository ratpHistoryRepository) {
        this.wsConsumer = wsConsumer;
        this.ratpHistoryRepository = ratpHistoryRepository;
    }

    protected void addToHistory(String description, Boolean isWorker) {
        RATPHistory ratpHistory = new RATPHistory(description, isWorker ? 2 : 1);

        this.ratpHistoryRepository.save(ratpHistory);
    }
}
