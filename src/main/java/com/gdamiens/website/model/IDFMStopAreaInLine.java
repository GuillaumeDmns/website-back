package com.gdamiens.website.model;

import com.gdamiens.website.model.id.IDFMStopAreaInLineId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity(name = "IDFMStopAreaInLine")
@Table(schema = "public", name = "idfm_stop_area_in_line")
@IdClass(IDFMStopAreaInLineId.class)
public class IDFMStopAreaInLine implements Serializable {

    @Id
    private String lineId;

    @Id
    private Integer stopAreaId;

    public IDFMStopAreaInLine() {
    }

    public IDFMStopAreaInLine(String lineId, Integer stopAreaId) {
        this.lineId = lineId;
        this.stopAreaId = stopAreaId;
    }

    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    public Integer getStopAreaId() {
        return stopAreaId;
    }

    public void setStopAreaId(Integer stopAreaId) {
        this.stopAreaId = stopAreaId;
    }

    @Override
    public String toString() {
        return "IDFMStopAreaInLine{" +
            "lineId='" + lineId + '\'' +
            ", stopAreaId=" + stopAreaId +
            '}';
    }
}
