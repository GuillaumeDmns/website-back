package com.gdamiens.website.repository;

import com.gdamiens.website.model.IDFMRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDFMRouteRepository extends JpaRepository<IDFMRoute, String> {
}
