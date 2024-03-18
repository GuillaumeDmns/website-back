package com.gdamiens.website.model;

import com.gdamiens.website.model.id.IDFMStopInLineId;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity(name = "IDFMStopInLine")
@Table(schema = "public", name = "idfm_stop_in_line")
@IdClass(IDFMStopInLineId.class)
public class IDFMStopInLine implements Serializable {

    @Id
    private String lineId;

    @Id
    private String stopId;

    public IDFMStopInLine() {
    }

    public IDFMStopInLine(String lineId, String stopId) {
        this.lineId = lineId;
        this.stopId = stopId;
    }

    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    @Override
    public String toString() {
        return "IDFMStopLine{" +
            "line_id='" + lineId + '\'' +
            ", stop_id=" + stopId +
            '}';
    }
}
