--liquibase formatted sql
-- GTFS IDFM tables. GtfsImportService clones this structure (columns, keys, indexes) into gtfs_new at each import,
-- so any change to the gtfs schema must be made by a new changeset here.

--changeset gdamiens:gtfs-baseline
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT count(*) FROM pg_namespace WHERE nspname = 'gtfs'
CREATE SCHEMA gtfs;

CREATE TABLE gtfs.agency (
    agency_id              text CONSTRAINT agency_pkey PRIMARY KEY,
    agency_name            text,
    agency_url             text,
    agency_timezone        text,
    agency_lang            text,
    agency_phone           text,
    agency_email           text,
    agency_fare_url        text,
    ticketing_deep_link_id text
);

CREATE TABLE gtfs.routes (
    route_id               text CONSTRAINT routes_pkey PRIMARY KEY,
    agency_id              text,
    route_short_name       text,
    route_long_name        text,
    route_desc             text,
    route_type             smallint,
    route_url              text,
    route_color            text,
    route_text_color       text,
    route_sort_order       integer,
    continuous_pickup      smallint,
    continuous_drop_off    smallint,
    network_id             text,
    ticketing_deep_link_id text
);
CREATE INDEX routes_agency_id_idx ON gtfs.routes (agency_id);

CREATE TABLE gtfs.trips (
    route_id              text,
    service_id            text,
    trip_id               text CONSTRAINT trips_pkey PRIMARY KEY,
    trip_headsign         text,
    trip_short_name       text,
    direction_id          smallint,
    block_id              text,
    shape_id              text,
    wheelchair_accessible smallint,
    bikes_allowed         smallint,
    cars_allowed          smallint
);
CREATE INDEX trips_route_id_idx ON gtfs.trips (route_id);
CREATE INDEX trips_service_id_idx ON gtfs.trips (service_id);
CREATE INDEX trips_shape_id_idx ON gtfs.trips (shape_id);

CREATE TABLE gtfs.calendar (
    service_id text CONSTRAINT calendar_pkey PRIMARY KEY,
    monday     boolean,
    tuesday    boolean,
    wednesday  boolean,
    thursday   boolean,
    friday     boolean,
    saturday   boolean,
    sunday     boolean,
    start_date date,
    end_date   date
);

CREATE TABLE gtfs.calendar_dates (
    service_id     text,
    date           date,
    exception_type smallint,
    CONSTRAINT calendar_dates_pkey PRIMARY KEY (service_id, date)
);
CREATE INDEX calendar_dates_date_idx ON gtfs.calendar_dates (date);

CREATE TABLE gtfs.stops (
    stop_id             text CONSTRAINT stops_pkey PRIMARY KEY,
    stop_code           text,
    stop_name           text,
    tts_stop_name       text,
    stop_desc           text,
    stop_lat            double precision,
    stop_lon            double precision,
    zone_id             text,
    stop_url            text,
    location_type       smallint,
    parent_station      text,
    stop_timezone       text,
    wheelchair_boarding smallint,
    level_id            text,
    platform_code       text,
    stop_access         smallint,
    geom                geometry(Point, 4326) GENERATED ALWAYS AS (ST_SetSRID(ST_MakePoint(stop_lon, stop_lat), 4326)) STORED
);
CREATE INDEX stops_parent_station_idx ON gtfs.stops (parent_station);
CREATE INDEX stops_geom_idx ON gtfs.stops USING gist (geom);

CREATE TABLE gtfs.stop_times (
    trip_id                      text,
    arrival_time                 interval,
    departure_time               interval,
    stop_id                      text,
    stop_sequence                integer,
    location_group_id            text,
    location_id                  text,
    start_pickup_drop_off_window interval,
    end_pickup_drop_off_window   interval,
    pickup_type                  smallint,
    drop_off_type                smallint,
    local_zone_id                text,
    stop_headsign                text,
    continuous_pickup            smallint,
    continuous_drop_off          smallint,
    shape_dist_traveled          double precision,
    timepoint                    smallint,
    pickup_booking_rule_id       text,
    drop_off_booking_rule_id     text,
    CONSTRAINT stop_times_pkey PRIMARY KEY (trip_id, stop_sequence)
);
CREATE INDEX stop_times_stop_id_departure_time_idx ON gtfs.stop_times (stop_id, departure_time);

CREATE TABLE gtfs.transfers (
    from_stop_id      text,
    to_stop_id        text,
    from_route_id     text,
    to_route_id       text,
    from_trip_id      text,
    to_trip_id        text,
    transfer_type     smallint,
    min_transfer_time integer
);
CREATE INDEX transfers_from_stop_id_idx ON gtfs.transfers (from_stop_id);

CREATE TABLE gtfs.pathways (
    pathway_id             text CONSTRAINT pathways_pkey PRIMARY KEY,
    from_stop_id           text,
    to_stop_id             text,
    pathway_mode           smallint,
    is_bidirectional       boolean,
    length                 double precision,
    traversal_time         integer,
    stair_count            integer,
    max_slope              double precision,
    min_width              double precision,
    signposted_as          text,
    reversed_signposted_as text
);
CREATE INDEX pathways_from_stop_id_idx ON gtfs.pathways (from_stop_id);

CREATE TABLE gtfs.shapes (
    shape_id text CONSTRAINT shapes_pkey PRIMARY KEY,
    geom     geometry(LineString, 4326)
);
CREATE INDEX shapes_geom_idx ON gtfs.shapes USING gist (geom);

CREATE TABLE gtfs.booking_rules (
    booking_rule_id           text CONSTRAINT booking_rules_pkey PRIMARY KEY,
    booking_type              smallint,
    prior_notice_duration_min integer,
    prior_notice_duration_max integer,
    prior_notice_last_day     integer,
    prior_notice_last_time    interval,
    prior_notice_start_day    integer,
    prior_notice_start_time   interval,
    prior_notice_service_id   text,
    message                   text,
    pickup_message            text,
    drop_off_message          text,
    phone_number              text,
    info_url                  text,
    booking_url               text
);

CREATE TABLE gtfs.ticketing_deep_links (
    ticketing_deep_link_id text CONSTRAINT ticketing_deep_links_pkey PRIMARY KEY,
    web_url                text,
    android_intent_uri     text,
    ios_universal_link_url text
);

CREATE TABLE gtfs.attributions (
    attribution_id    text,
    agency_id         text,
    route_id          text,
    trip_id           text,
    is_producer       smallint,
    is_operator       smallint,
    is_authority      smallint,
    organization_name text,
    attribution_url   text,
    attribution_email text,
    attribution_phone text
);
CREATE INDEX attributions_route_id_idx ON gtfs.attributions (route_id);

CREATE TABLE gtfs.object_codes_extension (
    object_type   text,
    object_id     text,
    object_system text,
    object_code   text
);
CREATE INDEX object_codes_extension_object_id_idx ON gtfs.object_codes_extension (object_id);
CREATE INDEX object_codes_extension_object_code_idx ON gtfs.object_codes_extension (object_code);
