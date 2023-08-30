package com.gdamiens.website.repository;

import com.gdamiens.website.model.IDFMStopOperator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface IDFMStopOperatorRepository extends JpaRepository<IDFMStopOperator, Integer> {

    @Transactional
    @Modifying
    @Query(value = "UPDATE idfm_stop_operator SET name = :name, latitude = :latitude, longitude = :longitude, type = :type WHERE id = :stopOperatorId", nativeQuery = true)
    int partialUpdate(@Param("stopOperatorId") Integer stopOperatorId, @Param("name") String name, @Param("latitude") Double latitude, @Param("longitude") Double longitude, @Param("type") String type);
}
