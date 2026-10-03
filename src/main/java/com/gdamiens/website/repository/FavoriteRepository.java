package com.gdamiens.website.repository;

import com.gdamiens.website.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUserIdOrderByIdAsc(Integer userId);

    Optional<Favorite> findByIdAndUserId(Long id, Integer userId);

    Optional<Favorite> findFirstByUserIdAndKind(Integer userId, Favorite.Kind kind);

    Optional<Favorite> findFirstByUserIdAndKindAndStopAreaId(Integer userId, Favorite.Kind kind, String stopAreaId);

    Optional<Favorite> findFirstByUserIdAndKindAndLineId(Integer userId, Favorite.Kind kind, String lineId);
}
