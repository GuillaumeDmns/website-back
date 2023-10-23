package com.gdamiens.website.controller.object.gtfs;

import com.opencsv.bean.CsvBindByPosition;

public class StopExtensionsCSV {

    @CsvBindByPosition(position = 0)
    private String object_id;

    @CsvBindByPosition(position = 1)
    private String object_system;

    @CsvBindByPosition(position = 2)
    private String object_code;

    public String getObject_id() {
        return object_id;
    }

    public void setObject_id(String object_id) {
        this.object_id = object_id;
    }

    public String getObject_system() {
        return object_system;
    }

    public void setObject_system(String object_system) {
        this.object_system = object_system;
    }

    public String getObject_code() {
        return object_code;
    }

    public void setObject_code(String object_code) {
        this.object_code = object_code;
    }
}
