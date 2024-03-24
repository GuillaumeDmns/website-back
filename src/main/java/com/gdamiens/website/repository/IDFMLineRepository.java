package com.gdamiens.website.repository;

import com.gdamiens.website.model.IDFMLine;
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

    @Transactional
    @Modifying
    @Query(value = "UPDATE idfm_line SET shape = ST_GeomFromGeoJSON(:geoJsonShape) WHERE id = :lineId", nativeQuery = true)
    void updateLineShape(@Param("lineId") String lineId, @Param("geoJsonShape") String geoJsonShape);
}
