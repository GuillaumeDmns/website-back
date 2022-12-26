package com.gdamiens.website.repository;

import com.gdamiens.website.model.IDFMStopArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDFMStopAreaRepository extends JpaRepository<IDFMStopArea, String> {
}
