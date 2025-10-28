package com.sky.movieratingservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.sky.movieratingservice.domain.repository")
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaConfig {
}
