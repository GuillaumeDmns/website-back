-- Exécuté après les COPY, avant la copie des clés et index depuis gtfs.

INSERT INTO gtfs_new.shapes (shape_id, geom)
SELECT shape_id,
       ST_MakeLine(ST_SetSRID(ST_MakePoint(shape_pt_lon, shape_pt_lat), 4326) ORDER BY shape_pt_sequence)
FROM gtfs_new.shape_points
GROUP BY shape_id;

-- Les points bruts ne servent qu'à construire shapes : la table reste vide pour être clonée au prochain import
TRUNCATE gtfs_new.shape_points;

INSERT INTO gtfs_new.route_stops (route_id, stop_id)
SELECT DISTINCT t.route_id, st.stop_id
FROM gtfs_new.stop_times st
JOIN gtfs_new.trips t ON t.trip_id = st.trip_id;
