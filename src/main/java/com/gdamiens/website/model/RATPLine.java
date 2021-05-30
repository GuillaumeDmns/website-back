package com.gdamiens.website.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gdamiens.website.ratp.wsdl.Line;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

@Entity(name = "RATPLine")
@Table(schema = "public", name = "ratp_line")
public class RATPLine implements Serializable {

    @Id
    private String id;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "code_stif")
    private String codeStif;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "image")
    private String image;

    @Column(name = "realm", nullable = false)
    private String realm;

    @Column(name = "reseau_id", nullable = false)
    private String reseauId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reseau_id", referencedColumnName = "id", nullable = false, insertable = false, updatable = false)
    private RATPReseau reseau;

    public RATPLine() {}

    public RATPLine(String id, String code, String codeStif, String name, String image, String realm, String reseauId) {
        this.id = id;
        this.code = code;
        this.codeStif = codeStif;
        this.name = name;
        this.image = image;
        this.realm = realm;
        this.reseauId = reseauId;
    }

    public RATPLine(Line line) {
        this.id = line.getId();
        this.code = line.getCode();
        this.codeStif = line.getCodeStif();
        this.name = line.getName();
        this.image = line.getImage();
        this.realm = line.getRealm();
        this.reseauId = line.getReseau().getId();
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

    public String getReseauId() {
        return reseauId;
    }

    public void setReseauId(String reseauId) {
        this.reseauId = reseauId;
    }

    public RATPReseau getReseau() {
        return reseau;
    }

    public void setReseau(RATPReseau reseau) {
        this.reseau = reseau;
    }

    @Override
    public String toString() {
        return "RATPLine{" +
                "id='" + id + '\'' +
                ", code='" + code + '\'' +
                ", codeStif='" + codeStif + '\'' +
                ", name='" + name + '\'' +
                ", image='" + image + '\'' +
                ", realm='" + realm + '\'' +
                ", reseauId='" + reseauId + '\'' +
                '}';
    }
}
