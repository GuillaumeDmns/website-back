package com.gdamiens.website.repository;


import com.gdamiens.website.model.RATPStation;
import com.gdamiens.website.model.RATPStationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface RATPStationRepository extends JpaRepository<RATPStation, RATPStationId> {
    List<RATPStation> getAllByLineId(String lineId);

}
