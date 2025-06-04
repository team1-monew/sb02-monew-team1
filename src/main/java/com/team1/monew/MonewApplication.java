package com.team1.monew;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MonewApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonewApplication.class, args);
	}

}
