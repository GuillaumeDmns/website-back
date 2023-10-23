package com.gdamiens.website.controller.object.gtfs;

import com.opencsv.bean.CsvBindByPosition;

public class AgencyCSV {

    @CsvBindByPosition(position = 0)
    private String agency_id;

    @CsvBindByPosition(position = 1)
    private String agency_name;

    @CsvBindByPosition(position = 2)
    private String agency_url;

    @CsvBindByPosition(position = 3)
    private String agency_timezone;

    @CsvBindByPosition(position = 4)
    private String agency_lang;

    @CsvBindByPosition(position = 5)
    private String agency_phone;

    @CsvBindByPosition(position = 6)
    private String agency_email;

    public String getAgency_id() {
        return agency_id;
    }

    public void setAgency_id(String agency_id) {
        this.agency_id = agency_id;
    }

    public String getAgency_name() {
        return agency_name;
    }

    public void setAgency_name(String agency_name) {
        this.agency_name = agency_name;
    }

    public String getAgency_url() {
        return agency_url;
    }

    public void setAgency_url(String agency_url) {
        this.agency_url = agency_url;
    }

    public String getAgency_timezone() {
        return agency_timezone;
    }

    public void setAgency_timezone(String agency_timezone) {
        this.agency_timezone = agency_timezone;
    }

    public String getAgency_lang() {
        return agency_lang;
    }

    public void setAgency_lang(String agency_lang) {
        this.agency_lang = agency_lang;
    }

    public String getAgency_phone() {
        return agency_phone;
    }

    public void setAgency_phone(String agency_phone) {
        this.agency_phone = agency_phone;
    }

    public String getAgency_email() {
        return agency_email;
    }

    public void setAgency_email(String agency_email) {
        this.agency_email = agency_email;
    }
}
