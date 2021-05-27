package com.gdamiens.website.repository;

import com.gdamiens.website.model.RATPHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RATPHistoryRepository extends JpaRepository<RATPHistory, Integer> {}
