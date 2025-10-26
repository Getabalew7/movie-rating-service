package com.sky.movieratingservice.domain.entity;

import com.sky.movieratingservice.domain.entity.common.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "USERS")
public class User extends Auditable {

    @Id
    @Column(unique = true, nullable = false, name = "USER_ID", updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(name = "EMAIL", nullable = false, unique = true, length = 50)
    private String email;

    @Column(name = "PASSWORD", nullable = false, length = 100)
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Rating> ratingList = new ArrayList<>();
}
