package com.doerapi.migrator;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.module.scala.DefaultScalaModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonConfiguration {
    @Bean
    public Module scalaModule() {
        return new DefaultScalaModule();
    }
}
