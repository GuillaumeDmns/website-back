package com.gdamiens.website.repository;

import com.gdamiens.website.model.IDFMRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IDFMRouteRepository extends JpaRepository<IDFMRoute, String> {

    @Query(value = "SELECT ST_AsGeoJSON(ST_Multi(ST_Collect(s.geom))) FROM (SELECT DISTINCT shape_id FROM gtfs.trips WHERE route_id = :routeId) t INNER JOIN gtfs.shapes s ON s.shape_id = t.shape_id", nativeQuery = true)
    String getShapeAsGeoJson(@Param("routeId") String routeId);
}
