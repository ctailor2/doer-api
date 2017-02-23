package com.doerapispring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
public class DoerApiSpringApplication {
	public static void main(String[] args) {
        SpringApplication.run(DoerApiSpringApplication.class, args);
	}

    // TODO: This is too global. Move closer to the database.
	@PostConstruct
	void started() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
}
