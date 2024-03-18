package com.gdamiens.website.repository;

import com.gdamiens.website.model.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TestRepository extends JpaRepository<Test, Integer> {

    @Transactional
    @Modifying
    @Query(value = "insert into test(id, geog) values (1, ST_SetSRID(ST_Point( :lat, :lon ), 4326)\\:\\:geography)", nativeQuery = true)
    void createOrUpdate(final Double lat, final Double lon);

}
