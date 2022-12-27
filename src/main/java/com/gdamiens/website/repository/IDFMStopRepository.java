package com.gdamiens.website.repository;

import com.gdamiens.website.model.IDFMStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface IDFMStopRepository extends JpaRepository<IDFMStop, Integer> {

    @Transactional
    @Modifying
    @Query(value = "UPDATE idfm_stop SET stop_area_id = :stopAreaId WHERE id = :stopId", nativeQuery = true)
    void setStopArea(@Param("stopAreaId") Integer stopAreaId, @Param("stopId") Integer stopId);

}
