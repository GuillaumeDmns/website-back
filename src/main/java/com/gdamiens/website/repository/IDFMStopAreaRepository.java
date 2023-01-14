package com.gdamiens.website.repository;

import com.gdamiens.website.model.IDFMStopArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDFMStopAreaRepository extends JpaRepository<IDFMStopArea, Integer> {

    @Query(value = "SELECT DISTINCT isa.* FROM idfm_stop_line isl INNER JOIN idfm_stop i ON i.id = isl.stop_id INNER JOIN idfm_stop_area isa ON isa.id = i.stop_area_id WHERE isl.line_id = :lineId ORDER BY isa.name", nativeQuery = true)
    List<IDFMStopArea> getStopAreasFromLineId(@Param("lineId") String lineId);

}
