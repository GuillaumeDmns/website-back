package com.gdamiens.website.repository;

import com.gdamiens.website.model.IDFMOperator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDFMOperatorRepository extends JpaRepository<IDFMOperator, Integer> {
}
