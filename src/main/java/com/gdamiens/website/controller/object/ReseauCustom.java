package com.gdamiens.website.controller.object;

import com.gdamiens.website.ratp.wsdl.Reseau;
import com.gdamiens.website.utils.Constants;

public class ReseauCustom {

    private String id;

    private String code;

    private String name;

    private String image;

    public ReseauCustom(Reseau reseau) {
        if (reseau != null) {
            this.id = reseau.getId();
            this.code = reseau.getCode();
            this.name = reseau.getName();
            this.image = reseau.getImage() == null ? null : Constants.ROOT_IMG_URL + reseau.getImage();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
