package com.hr.hrapp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.hr.hrapp.entity.User;
import com.hr.hrapp.repository.UserRepository;

@SpringBootApplication
public class HrappApplication {
 
	public static void main(String[] args) {
		SpringApplication.run(HrappApplication.class, args);
	}
	@Bean
	CommandLineRunner run(UserRepository repo, BCryptPasswordEncoder encoder) {
	    return args -> {

	        if (repo.findByUsername("admin") == null) {
	            User admin = new User();
	            admin.setUsername("admin");
	            admin.setPassword(encoder.encode("admin123"));
	            admin.setRole("ADMIN");
	            repo.save(admin);
	        }

	        if (repo.findByUsername("user") == null) {
	            User user = new User();
	            user.setUsername("user");
	            user.setPassword(encoder.encode("user123"));
	            user.setRole("USER");
	            repo.save(user);
	        }
	    };
	}

}
