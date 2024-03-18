package com.gdamiens.website.repository;

import com.gdamiens.website.model.IDFMStopGtfs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDFMStopGtfsRepository extends JpaRepository<IDFMStopGtfs, String> {

    @Query(value = "SELECT DISTINCT s.* FROM idfm_stop_in_line isil INNER JOIN idfm_stop_gtfs s ON s.id = isil.stop_id WHERE isil.line_id = :lineId ORDER BY s.name", nativeQuery = true)
    List<IDFMStopGtfs> getStopsFromLineId(@Param("lineId") String lineId);

    @Query(value = "SELECT s2.id, s2.latitude, s2.longitude, s.name, s2.parent_station FROM idfm_stop_in_line isil INNER JOIN idfm_stop_gtfs s ON s.id = isil.stop_id INNER JOIN idfm_stop_gtfs s2 on s2.id = s.parent_station WHERE isil.line_id = :lineId group by s.name, s2.id, s2.latitude, s2.longitude, s2.parent_station ORDER BY s2.name", nativeQuery = true)
    List<IDFMStopGtfs> getParentStopsFromLineId(@Param("lineId") String lineId);
}
