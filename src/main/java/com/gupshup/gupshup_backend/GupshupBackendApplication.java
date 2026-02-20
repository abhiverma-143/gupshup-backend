package com.gupshup.gupshup_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GupshupBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(GupshupBackendApplication.class, args);
	}

}
