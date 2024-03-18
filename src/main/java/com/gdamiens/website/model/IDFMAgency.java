package com.gdamiens.website.model;

import com.gdamiens.website.controller.object.OperatorsCSV;
import com.gdamiens.website.controller.object.gtfs.AgencyCSV;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity(name = "IDFMAgency")
@Table(schema = "public", name = "idfm_agency")
public class IDFMAgency implements Serializable {

    @Id
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "url")
    private String url;

    @Column(name = "timezone")
    private String timezone;

    public IDFMAgency() {
    }

    public IDFMAgency(AgencyCSV agencyCSV) {
        this.id = agencyCSV.getAgency_id();
        this.name = agencyCSV.getAgency_name();
        this.url = agencyCSV.getAgency_url();
        this.timezone = agencyCSV.getAgency_timezone();
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    @Override
    public String toString() {
        return "IDFMAgency{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", url='" + url + '\'' +
            ", timezone='" + timezone + '\'' +
            '}';
    }
}
