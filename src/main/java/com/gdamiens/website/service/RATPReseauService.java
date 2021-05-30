package com.gdamiens.website.service;

import com.gdamiens.website.controller.object.ReseauxDTO;
import com.gdamiens.website.model.RATPReseau;
import com.gdamiens.website.ratp.WsConsumer;
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

    public ReseauxDTO getReseaux() {
        return new ReseauxDTO(this.reseauRepository.findAll());
    }

    public void refreshReseau(Reseau reseau) {
        if (reseau != null) {
            reseauRepository.save(new RATPReseau(
                    reseau.getId(),
                    reseau.getCode(),
                    reseau.getName(),
                    reseau.getImage())
            );
        }
    }
}
