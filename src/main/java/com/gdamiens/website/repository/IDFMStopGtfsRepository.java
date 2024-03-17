package com.gdamiens.website.repository;

import com.gdamiens.website.model.IDFMStopGtfs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface IDFMStopGtfsRepository extends JpaRepository<IDFMStopGtfs, String> {

    @Transactional
    @Modifying
    @Query(value = "UPDATE idfm_stop_gtfs SET route_id = :routeId WHERE id = :stopId", nativeQuery = true)
    void updateRouteId(@Param("stopId") String stopId, @Param("routeId") String routeId);
}
