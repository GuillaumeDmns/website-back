package com.gdamiens.website.controller.object.gtfs;

import com.opencsv.bean.CsvBindByPosition;

public class TransfersCSV {
    @CsvBindByPosition(position = 0)
    private String from_stop_id;

    @CsvBindByPosition(position = 1)
    private String to_stop_id;

    @CsvBindByPosition(position = 2)
    private Integer transfer_type;

    @CsvBindByPosition(position = 3)
    private Integer min_transfer_time;

    public String getFrom_stop_id() {
        return from_stop_id;
    }

    public void setFrom_stop_id(String from_stop_id) {
        this.from_stop_id = from_stop_id;
    }

    public String getTo_stop_id() {
        return to_stop_id;
    }

    public void setTo_stop_id(String to_stop_id) {
        this.to_stop_id = to_stop_id;
    }

    public Integer getTransfer_type() {
        return transfer_type;
    }

    public void setTransfer_type(Integer transfer_type) {
        this.transfer_type = transfer_type;
    }

    public Integer getMin_transfer_time() {
        return min_transfer_time;
    }

    public void setMin_transfer_time(Integer min_transfer_time) {
        this.min_transfer_time = min_transfer_time;
    }
}
