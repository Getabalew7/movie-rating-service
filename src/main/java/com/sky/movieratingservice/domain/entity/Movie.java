package com.sky.movieratingservice.domain.entity;

import com.sky.movieratingservice.domain.entity.common.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "MOVIES")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Movie extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "MOVIE_ID", unique = true, nullable = false, updatable = false)
    private UUID id;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "DIRECTOR", length = 100, nullable = false)
    private String director;

    @Column(name = "GENRE", length = 50)
    private String genre;

    @Column(name = "DESCRIPTION", length = 100)
    private String description;

    @Column(name = "RELEASE_YEAR", nullable = false)
    private  Integer releaseYear;


    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Rating> ratings = new ArrayList<>();



}
