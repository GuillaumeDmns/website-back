--liquibase formatted sql

--changeset gdamiens:gtfs-route-stops
--comment: Stops served by each route, derived from stop_times at each GTFS import (gtfs/post_load.sql)
CREATE TABLE gtfs.route_stops (
    route_id text,
    stop_id  text,
    CONSTRAINT route_stops_pkey PRIMARY KEY (route_id, stop_id)
);

--changeset gdamiens:gtfs-route-stops-fill
--comment: Fill route_stops for the GTFS already loaded, the next imports rebuild it
INSERT INTO gtfs.route_stops (route_id, stop_id)
SELECT DISTINCT t.route_id, st.stop_id
FROM gtfs.stop_times st
JOIN gtfs.trips t ON t.trip_id = st.trip_id;
