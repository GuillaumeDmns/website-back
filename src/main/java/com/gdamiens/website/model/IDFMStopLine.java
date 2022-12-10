package com.gdamiens.website.model;

import com.gdamiens.website.model.id.IDFMStopLineId;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity(name = "IDFMStopLine")
@Table(schema = "public", name = "idfm_stop_line")
@IdClass(IDFMStopLineId.class)
public class IDFMStopLine implements Serializable {

    @Id
    private String lineId;

    @Id
    private Integer stopId;

    public IDFMStopLine() {
    }

    public IDFMStopLine(String lineId, Integer stopId) {
        this.lineId = lineId;
        this.stopId = stopId;
    }

    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    public Integer getStopId() {
        return stopId;
    }

    public void setStopId(Integer stopId) {
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
