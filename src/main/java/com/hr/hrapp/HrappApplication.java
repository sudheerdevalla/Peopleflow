package com.hr.hrapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HrappApplication {

	public static void main(String[] args) {
		SpringApplication.run(HrappApplication.class, args);
	}

}
