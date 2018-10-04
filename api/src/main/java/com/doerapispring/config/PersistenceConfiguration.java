package com.doerapispring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.util.IdGenerator;
import org.springframework.util.JdkIdGenerator;

@Configuration
@EnableJpaAuditing
public class PersistenceConfiguration {
    @Bean
    public IdGenerator idGenerator() {
        return new JdkIdGenerator();
    }
}
