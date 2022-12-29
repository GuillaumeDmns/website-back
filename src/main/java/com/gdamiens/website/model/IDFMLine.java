package com.gdamiens.website.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity(name = "IDFMLine")
@Table(schema = "public", name = "idfm_line")
public class IDFMLine implements Serializable {

    @Id
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "transport_mode")
    @Enumerated(EnumType.STRING)
    private TransportMode transportMode;

    @Column(name = "operator_id")
    private Integer operatorId;

    @Column(name = "line_id_color")
    private String lineIdColor;

    @Column(name = "line_id_background_color")
    private String lineIdBackgroundColor;

    public IDFMLine() {
    }

    public IDFMLine(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TransportMode getTransportMode() {
        return transportMode;
    }

    public void setTransportMode(TransportMode transportMode) {
        this.transportMode = transportMode;
    }

    public Integer getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
    }

    public String getLineIdColor() {
        return lineIdColor;
    }

    public void setLineIdColor(String lineIdColor) {
        this.lineIdColor = lineIdColor;
    }

    public String getLineIdBackgroundColor() {
        return lineIdBackgroundColor;
    }

    public void setLineIdBackgroundColor(String lineIdBackgroundColor) {
        this.lineIdBackgroundColor = lineIdBackgroundColor;
    }

    @Override
    public String toString() {
        return "IDFMLine{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", transportMode='" + transportMode + '\'' +
            ", operatorId=" + operatorId +
            ", lineIdColor='" + lineIdColor + '\'' +
            ", lineIdBackgroundColor='" + lineIdBackgroundColor + '\'' +
            '}';
    }
}
