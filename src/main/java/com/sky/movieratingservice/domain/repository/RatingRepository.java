package com.sky.movieratingservice.domain.repository;

import com.sky.movieratingservice.domain.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    @Query("""
            select avg(r.ratingValue) from Rating r
            where r.movie.id = :movieID
            """)
    Optional<Double> findAverageRatingByMovieId(UUID movieID);

    @Query("""
            select r from Rating r
            join fetch r.movie
            join fetch r.user
            where r.user.id = :userId
            order by r.createdAt desc
            """)
    List<Rating> findByUserIdWithDetails(UUID userId);
}
