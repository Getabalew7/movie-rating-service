package com.sky.movieratingservice.domain.repository;

import com.sky.movieratingservice.domain.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UUID> {
    Optional<Rating> findByUserIdAndMovieId(UUID userID, UUID movieID);

    List<Rating> findByUserId(UUID userID);

    List<Rating> findByMovieId(UUID movieID);

    long countByMovieId(UUID movieID);

    Optional<Double> findAverageRatingByMovieId(UUID movieID);

}
