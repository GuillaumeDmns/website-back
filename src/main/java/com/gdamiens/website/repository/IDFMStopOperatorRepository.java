package com.gdamiens.website.repository;

import com.gdamiens.website.model.IDFMStopOperator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDFMStopOperatorRepository extends JpaRepository<IDFMStopOperator, Integer> {

}
