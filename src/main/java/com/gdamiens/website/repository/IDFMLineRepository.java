package com.gdamiens.website.repository;

import com.gdamiens.website.model.IDFMLine;
import org.locationtech.jts.geom.MultiLineString;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface IDFMLineRepository extends JpaRepository<IDFMLine, String> {

    List<IDFMLine> findAllByOrderByNameAsc();

    @Query(value = "SELECT l.id, l.name, l.transport_mode, l.operator_id, l.line_id_color, l.line_id_background_color, null AS shape FROM idfm_line l", nativeQuery = true)
    List<IDFMLine> findAllByOrderByNameAscWithoutShape();

    @Transactional
    @Modifying
    @Query(value = "UPDATE idfm_line SET shape = ST_GeomFromGeoJSON(:geoJsonShape) WHERE id = :lineId", nativeQuery = true)
    void updateBusShape(@Param("lineId") String lineId, @Param("geoJsonShape") String geoJsonShape);

    @Transactional
    @Modifying
    @Query(value = "UPDATE idfm_line SET shape = :multilineString WHERE id = :lineId", nativeQuery = true)
    void updateRailShape(@Param("lineId") String lineId, @Param("multilineString") MultiLineString multilineString);
}
