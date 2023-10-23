package com.gdamiens.website.controller.object.gtfs;

import com.opencsv.bean.CsvBindByPosition;

public class TripsCSV {

    @CsvBindByPosition(position = 0)
    private String route_id;

    @CsvBindByPosition(position = 1)
    private String service_id;

    @CsvBindByPosition(position = 2)
    private String trip_id;

    @CsvBindByPosition(position = 3)
    private String trip_headsign;

    @CsvBindByPosition(position = 4)
    private Integer trip_short_name;

    @CsvBindByPosition(position = 5)
    private Boolean direction_id;

    @CsvBindByPosition(position = 6)
    private String block_id;

    @CsvBindByPosition(position = 7)
    private String shape_id;

    @CsvBindByPosition(position = 8)
    private Integer wheelchair_accessible;

    @CsvBindByPosition(position = 9)
    private Integer bikes_allowed;
}
