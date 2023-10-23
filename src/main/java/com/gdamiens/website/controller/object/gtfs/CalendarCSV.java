package com.gdamiens.website.controller.object.gtfs;

import com.opencsv.bean.CsvBindByPosition;

public class CalendarCSV {
    @CsvBindByPosition(position = 0)
    private String service_id;

    @CsvBindByPosition(position = 1)
    private Boolean monday;

    @CsvBindByPosition(position = 2)
    private Boolean tuesday;

    @CsvBindByPosition(position = 3)
    private Boolean wednesday;

    @CsvBindByPosition(position = 4)
    private Boolean thursday;

    @CsvBindByPosition(position = 5)
    private Boolean friday;

    @CsvBindByPosition(position = 6)
    private Boolean saturday;

    @CsvBindByPosition(position = 7)
    private Boolean sunday;

    @CsvBindByPosition(position = 8)
    private String start_day;

    @CsvBindByPosition(position = 9)
    private String end_date;

    public String getService_id() {
        return service_id;
    }

    public void setService_id(String service_id) {
        this.service_id = service_id;
    }

    public Boolean getMonday() {
        return monday;
    }

    public void setMonday(Boolean monday) {
        this.monday = monday;
    }

    public Boolean getTuesday() {
        return tuesday;
    }

    public void setTuesday(Boolean tuesday) {
        this.tuesday = tuesday;
    }

    public Boolean getWednesday() {
        return wednesday;
    }

    public void setWednesday(Boolean wednesday) {
        this.wednesday = wednesday;
    }

    public Boolean getThursday() {
        return thursday;
    }

    public void setThursday(Boolean thursday) {
        this.thursday = thursday;
    }

    public Boolean getFriday() {
        return friday;
    }

    public void setFriday(Boolean friday) {
        this.friday = friday;
    }

    public Boolean getSaturday() {
        return saturday;
    }

    public void setSaturday(Boolean saturday) {
        this.saturday = saturday;
    }

    public Boolean getSunday() {
        return sunday;
    }

    public void setSunday(Boolean sunday) {
        this.sunday = sunday;
    }

    public String getStart_day() {
        return start_day;
    }

    public void setStart_day(String start_day) {
        this.start_day = start_day;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }
}
