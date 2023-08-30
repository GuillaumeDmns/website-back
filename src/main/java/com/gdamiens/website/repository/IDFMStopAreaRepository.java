package com.gdamiens.website.repository;

import com.gdamiens.website.model.IDFMStopArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface IDFMStopAreaRepository extends JpaRepository<IDFMStopArea, Integer> {

    @Query(value = "SELECT DISTINCT isa.* FROM idfm_stop_in_line isil INNER JOIN idfm_stop s ON s.id = isil.stop_id INNER JOIN idfm_stop_area isa ON isa.id = s.stop_area_id WHERE isil.line_id = :lineId ORDER BY isa.name", nativeQuery = true)
    List<IDFMStopArea> getStopAreasFromLineId(@Param("lineId") String lineId);

    @Query(value = "SELECT DISTINCT isa.* FROM idfm_stop_area_in_line isail INNER JOIN idfm_stop_area isa ON isa.id = isail.stop_area_id WHERE isail.line_id = :lineId ORDER BY isa.name", nativeQuery = true)
    List<IDFMStopArea> getRailStopAreasFromLineId(@Param("lineId") String lineId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE idfm_stop_area SET name = :name, latitude = :latitude, longitude = :longitude, type = :type WHERE id = :stopAreaId", nativeQuery = true)
    int partialUpdate(@Param("stopAreaId") Integer stopAreaId, @Param("name") String name, @Param("latitude") Double latitude, @Param("longitude") Double longitude, @Param("type") String type);

}
