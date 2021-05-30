package com.gdamiens.website.controller.object;

import com.gdamiens.website.model.RATPReseau;

import java.util.List;


public class ReseauxDTO extends RATPResponse {

    private List<RATPReseau> reseaux;

    public ReseauxDTO(List<RATPReseau> reseaux) {
        super();
        this.reseaux = reseaux;
    }

    public List<RATPReseau> getReseaux() {
        return reseaux;
    }

    public void setReseaux(List<RATPReseau> reseaux) {
        this.reseaux = reseaux;
    }
}
