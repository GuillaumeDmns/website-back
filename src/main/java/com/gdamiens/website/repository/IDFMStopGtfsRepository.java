package com.gdamiens.website.repository;

import com.gdamiens.website.model.IDFMStopGtfs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDFMStopGtfsRepository extends JpaRepository<IDFMStopGtfs, String> {

    @Query(value = "SELECT DISTINCT s.stop_id, s.stop_name, s.stop_lat, s.stop_lon, s.parent_station FROM gtfs.route_stops rs INNER JOIN gtfs.stops s ON s.stop_id = rs.stop_id WHERE rs.route_id = :routeId ORDER BY s.stop_name", nativeQuery = true)
    List<IDFMStopGtfs> getStopsFromRouteId(@Param("routeId") String routeId);

    @Query(value = "SELECT s2.stop_id, s2.stop_lat, s2.stop_lon, s.stop_name, s2.parent_station FROM gtfs.route_stops rs INNER JOIN gtfs.stops s ON s.stop_id = rs.stop_id INNER JOIN gtfs.stops s2 on s2.stop_id = s.parent_station WHERE rs.route_id = :routeId group by s.stop_name, s2.stop_id, s2.stop_lat, s2.stop_lon, s2.parent_station ORDER BY s2.stop_name", nativeQuery = true)
    List<IDFMStopGtfs> getParentStopsFromRouteId(@Param("routeId") String routeId);
}
