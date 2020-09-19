package com.doerapispring.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.module.scala.DefaultScalaModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableHypermediaSupport;

import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.HAL;

@Configuration
@EnableHypermediaSupport(type = HAL)
public class JsonConfiguration {
    @Bean
    public Module scalaModule() {
        return new DefaultScalaModule();
    }
}
