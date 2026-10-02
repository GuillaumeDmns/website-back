package com.gdamiens.website.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;

@Entity(name = "IDFMAgency")
@Table(schema = "gtfs", name = "agency")
public class IDFMAgency implements Serializable {

    @Id
    @Column(name = "agency_id")
    private String id;

    @Column(name = "agency_name")
    private String name;

    @Column(name = "agency_url")
    private String url;

    @Column(name = "agency_timezone")
    private String timezone;

    public IDFMAgency() {
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
