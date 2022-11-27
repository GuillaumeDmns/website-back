package com.gdamiens.website.repository;

import com.gdamiens.website.model.IDFMStopLine;
import com.gdamiens.website.model.id.IDFMStopLineId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDFMStopLineRepository extends JpaRepository<IDFMStopLine, IDFMStopLineId> {
}
