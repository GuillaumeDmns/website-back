package com.gdamiens.website.service;

import com.gdamiens.website.model.RATPReseau;
import com.gdamiens.website.ratp.WsConsumer;
import com.gdamiens.website.ratp.wsdl.GetLinesResponse;
import com.gdamiens.website.ratp.wsdl.Reseau;
import com.gdamiens.website.repository.RATPHistoryRepository;
import com.gdamiens.website.repository.RATPReseauRepository;
import org.springframework.stereotype.Service;

@Service
public class RATPReseauService extends RATPService {

    private final RATPReseauRepository reseauRepository;

    public RATPReseauService(WsConsumer wsConsumer, RATPHistoryRepository ratpHistoryRepository, RATPReseauRepository reseauRepository) {
        super(wsConsumer, ratpHistoryRepository);
        this.reseauRepository = reseauRepository;
    }

    public void refreshReseau() {
        GetLinesResponse response = wsConsumer.getLines(null, "*", null, null, null);

        if (response != null && response.getReturn() != null) {
            response.getReturn().forEach(line -> {
                Reseau reseau = line.getReseau();
                if (reseau != null) {
                    reseauRepository.save(new RATPReseau(
                            reseau.getId(),
                            reseau.getCode(),
                            reseau.getName(),
                            reseau.getImage())
                    );
                }
            });
        }

        this.addToHistory("RefreshReseau");
    }
}
