package com.gdamiens.website.repository;

import com.gdamiens.website.model.RATPLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RATPLineRepository  extends JpaRepository<RATPLine, String> {
}
