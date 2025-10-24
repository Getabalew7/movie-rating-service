package com.sky.movieratingservice;

import org.springframework.boot.SpringApplication;

public class TestMovieRatingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(MovieRatingServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
