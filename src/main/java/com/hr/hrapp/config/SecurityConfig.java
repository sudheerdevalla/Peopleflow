package com.hr.hrapp.config;

import org.springframework.context.annotation.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.hr.hrapp.service.CustomerUserDetailsService;

//import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class SecurityConfig {

	@Autowired
	private CustomerUserDetailsService userDetailsService;

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http.csrf(csrf -> csrf.disable())

		.authorizeHttpRequests(auth -> auth
			    .requestMatchers("/", "/login", "/register", "/error",
			                     "/images/**",
			                     "/*/.css", "/*/.js")
			    .permitAll()
			    .requestMatchers("/admin/**").hasRole("ADMIN")
			    .requestMatchers("/user/**").hasRole("USER")
			    .anyRequest().authenticated()
			)

				.formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/default", true).permitAll())

				.logout(logout -> logout.logoutSuccessUrl("/login").permitAll());

		return http.build();
	}
	
	


}
