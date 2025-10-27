package com.sky.movieratingservice.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sky.movieratingservice.domain.entity.common.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "RATINGS")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"user", "movie"})
@EqualsAndHashCode(exclude = {"user", "movie"}, callSuper = false)
public class Rating  extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "RATING_ID", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MOVIE_ID", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_ID", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "RATING_VALUE", nullable = false)
    private Integer ratingValue;

    @Column(name = "REVIEW", length = 2000)
    private String review;


}
