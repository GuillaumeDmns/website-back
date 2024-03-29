package com.gdamiens.website.repository;

import com.gdamiens.website.model.IDFMTrip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDFMTripRepository extends JpaRepository<IDFMTrip, String> {
}
