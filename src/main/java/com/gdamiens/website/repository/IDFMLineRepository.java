package com.gdamiens.website.repository;

import com.gdamiens.website.model.IDFMLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDFMLineRepository extends JpaRepository<IDFMLine, String> {
}
