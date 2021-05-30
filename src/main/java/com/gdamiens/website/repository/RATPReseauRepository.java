package com.gdamiens.website.repository;

import com.gdamiens.website.model.RATPReseau;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RATPReseauRepository extends JpaRepository<RATPReseau, String> {}
