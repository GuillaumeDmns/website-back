package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.LinesDTO;
import com.gdamiens.website.model.RATPLine;
import com.gdamiens.website.ratp.WsConsumer;
import com.gdamiens.website.ratp.wsdl.GetLinesResponse;
import com.gdamiens.website.repository.RATPHistoryRepository;
import com.gdamiens.website.repository.RATPLineRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RATPLineService extends RATPService {

    private final RATPReseauService ratpReseauService;

    private final RATPLineRepository ratpLineRepository;

    private final RATPStationService ratpStationService;

    public RATPLineService(WsConsumer wsConsumer,
                           RATPHistoryRepository ratpHistoryRepository,
                           RATPReseauService ratpReseauService,
                           RATPLineRepository ratpLineRepository,
                           RATPStationService ratpStationService) {
        super(wsConsumer, ratpHistoryRepository);
        this.ratpReseauService = ratpReseauService;
        this.ratpLineRepository = ratpLineRepository;
        this.ratpStationService = ratpStationService;
    }

    public LinesDTO getLinesByReseauId(String reseauId) {
        List<RATPLine> ratpLines = ratpLineRepository.findAllByReseauId(reseauId);
        return new LinesDTO(ratpLines);
    }

    public void refreshLines(Boolean isWorker) {
        GetLinesResponse response = this.wsConsumer.getLines(null, "*", null, "r", null);

        if (response != null && response.getReturn() != null) {
            response.getReturn().forEach(line -> {
                this.ratpReseauService.refreshReseau(line.getReseau());
                this.ratpLineRepository.save(new RATPLine(line));
                this.ratpStationService.deactivateStationsByLine(line.getId());
                this.ratpStationService.getStationsNoDTO(line.getId(), "*")
                        .forEach(station -> {
                            station.setActive(true);
                            station.setCoordinate("todo");
                            this.ratpStationService.save(station);
                        });
            });
        }

        this.addToHistory("RefreshLines", isWorker);
    }
}
