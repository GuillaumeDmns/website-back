package com.gdamiens.website.repository;

import com.gdamiens.website.model.IDFMAgency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDFMAgencyRepository extends JpaRepository<IDFMAgency, String> {
}
