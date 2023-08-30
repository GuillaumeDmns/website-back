package com.gdamiens.website.repository;

import com.gdamiens.website.model.IDFMStopAreaInLine;
import com.gdamiens.website.model.id.IDFMStopAreaInLineId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDFMStopAreaInLineRepository extends JpaRepository<IDFMStopAreaInLine, IDFMStopAreaInLineId> {
}
