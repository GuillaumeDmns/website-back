package com.gdamiens.website.repository;

import com.gdamiens.website.model.IDFMStopInLine;
import com.gdamiens.website.model.id.IDFMStopInLineId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDFMStopInLineRepository extends JpaRepository<IDFMStopInLine, IDFMStopInLineId> {
}
