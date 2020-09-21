package com.doerapispring;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Migrator implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(Migrator.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

    }
}
