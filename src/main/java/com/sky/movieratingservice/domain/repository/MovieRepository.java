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
            select m, coalesce(avg(r.ratingValue),0) as avgRating from Movie m
                        left join Rating  r on m.id=r.movie.id
                        group by m.id
                        order by avgRating desc NULLS LAST limit 1
            
            """)
    List<Object[]> findMoviesAndAverageRatings();

    @Query("""
            SELECT m.id as movieId,
                   m.name as movieName,
                   m.description as movieDescription,
                   m.releaseYear as releaseYear,
                   m.genre as genre,
                   m.director as director,
                   COALESCE(AVG(r.ratingValue), 0) as avgRating,
                   COUNT(r.id) as ratingCount
            FROM Movie m
            LEFT JOIN m.ratings r
            GROUP BY m.id, m.name, m.description, m.releaseYear, m.genre, m.director
            HAVING COUNT(r.id) > 0
            ORDER BY avgRating DESC, ratingCount DESC
            """)
    Page<MovieStatistics> findMoviesWithStatistics(Pageable pageable);

    @Query("""
        SELECT m.id as movieId,
               m.name as movieName,
               m.description as movieDescription,
               m.releaseYear as releaseYear,
               m.genre as genre,
               m.director as director,
               AVG(r.ratingValue) as avgRating,
               COUNT(r.id) as ratingCount
        FROM Movie m
        INNER JOIN m.ratings r
        GROUP BY m.id, m.name, m.description, m.releaseYear, m.genre, m.director
        HAVING COUNT(r.id) >= :minRatings
        ORDER BY avgRating DESC, ratingCount DESC
        """)
    Page<MovieStatistics> findTopRatedMovies(@Param("minRatings") long minRatings, Pageable pageable);

    // Projection interface for statistics
    interface MovieStatistics {
        UUID getMovieId();
        String getMovieName();
        String getMovieDescription();
        Integer getReleaseYear();
        String getGenre();
        String getDirector();
        Double getAvgRating();
        Long getRatingCount();
    }
}
