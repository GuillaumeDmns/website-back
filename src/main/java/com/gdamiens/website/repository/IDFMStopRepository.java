package com.gdamiens.website.repository;

import com.gdamiens.website.model.IDFMStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDFMStopRepository extends JpaRepository<IDFMStop, Integer> {
}
