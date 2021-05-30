package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.LinesDTO;
import com.gdamiens.website.ratp.WsConsumer;
import com.gdamiens.website.ratp.wsdl.GetLinesResponse;
import com.gdamiens.website.repository.RATPHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class RATPLineService extends RATPService {

    public RATPLineService(WsConsumer wsConsumer, RATPHistoryRepository ratpHistoryRepository) {
        super(wsConsumer, ratpHistoryRepository);
    }

    public LinesDTO getLinesInfos(String lineId, String code, String codeStif, String realm, String codeReseau) {
        GetLinesResponse response = wsConsumer.getLines(lineId, code, codeStif, realm, codeReseau);

        if (response != null && response.getReturn().size() > 0 && response.getReturn().get(0) != null) {
            return new LinesDTO(response.getReturn());
        }

        this.addToHistory("GetLinesInfos");

        return new LinesDTO(new ArrayList<>());
    }
}
