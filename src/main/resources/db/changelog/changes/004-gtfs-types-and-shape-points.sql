--liquibase formatted sql

--changeset gdamiens:gtfs-restore-types
--comment: Hibernate ddl-auto=update had turned the entity-mapped GTFS columns into varchar/integer; restore the baseline types (no-op on a fresh database)
ALTER TABLE gtfs.agency
    ALTER COLUMN agency_id TYPE text,
    ALTER COLUMN agency_name TYPE text,
    ALTER COLUMN agency_url TYPE text,
    ALTER COLUMN agency_timezone TYPE text;
ALTER TABLE gtfs.routes
    ALTER COLUMN route_id TYPE text,
    ALTER COLUMN agency_id TYPE text,
    ALTER COLUMN route_short_name TYPE text,
    ALTER COLUMN route_long_name TYPE text,
    ALTER COLUMN route_type TYPE smallint,
    ALTER COLUMN route_color TYPE text,
    ALTER COLUMN route_text_color TYPE text;
ALTER TABLE gtfs.trips
    ALTER COLUMN route_id TYPE text,
    ALTER COLUMN service_id TYPE text,
    ALTER COLUMN trip_id TYPE text,
    ALTER COLUMN trip_headsign TYPE text,
    ALTER COLUMN trip_short_name TYPE text,
    ALTER COLUMN direction_id TYPE smallint,
    ALTER COLUMN wheelchair_accessible TYPE smallint,
    ALTER COLUMN bikes_allowed TYPE smallint;
ALTER TABLE gtfs.stops
    ALTER COLUMN stop_id TYPE text,
    ALTER COLUMN stop_name TYPE text,
    ALTER COLUMN parent_station TYPE text;

--changeset gdamiens:gtfs-shape-points
--comment: Staging table for shapes.txt, kept empty in gtfs so that the import can clone it
CREATE TABLE gtfs.shape_points (
    shape_id            text,
    shape_pt_lat        double precision,
    shape_pt_lon        double precision,
    shape_pt_sequence   integer,
    shape_dist_traveled double precision
);
