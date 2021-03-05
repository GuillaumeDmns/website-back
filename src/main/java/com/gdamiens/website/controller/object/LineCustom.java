package com.gdamiens.website.controller.object;

import com.gdamiens.website.ratp.wsdl.Line;
import com.gdamiens.website.ratp.wsdl.Reseau;
import com.gdamiens.website.utils.Constants;

public class LineCustom {

    private String id;

    private String code;

    private String codeStif;

    private String name;

    private String image;

    private String realm;

    private ReseauCustom reseau;

    public LineCustom(Line line) {
        if (line != null) {
            this.id = line.getId();
            this.code = line.getCode();
            this.codeStif = line.getCodeStif();
            this.name = line.getName();
            this.image = line.getImage() == null ? null : Constants.ROOT_IMG_URL + line.getImage();
            this.realm = line.getRealm();
            this.reseau = new ReseauCustom(line.getReseau());
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

    public String getCodeStif() {
        return codeStif;
    }

    public void setCodeStif(String codeStif) {
        this.codeStif = codeStif;
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

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public ReseauCustom getReseau() {
        return reseau;
    }

    public void setReseau(ReseauCustom reseau) {
        this.reseau = reseau;
    }
}
