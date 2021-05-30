package com.gdamiens.website.repository;

import com.gdamiens.website.model.RATPLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RATPLineRepository  extends JpaRepository<RATPLine, String> {
    List<RATPLine> findAllByReseauId(String reseauId);
}
