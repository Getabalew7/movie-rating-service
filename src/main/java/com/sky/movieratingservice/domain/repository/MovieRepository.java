package com.sky.movieratingservice.domain.repository;

import com.sky.movieratingservice.domain.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MovieRepository extends JpaRepository<Movie, UUID> {
    Page<Movie> findAll(Pageable pageable);
    @Query("""
              SELECT m FROM Movie m
              LEFT JOIN FETCH m.ratings r
              WHERE m.id = :movie_id
              """)
    Optional<Movie> findByIdWithRatings(@Param("movie_id") UUID movieId);

    @Query("""
            select m, avg(r.ratingValue) as avgRating from Movie m
                        left join Rating  r on m.id=r.movie.id
                        group by m.id
                        order by avgRating desc NULLS LAST 
            
            """)
    List<Object[]> findMoviesAndAverageRatings();
}
