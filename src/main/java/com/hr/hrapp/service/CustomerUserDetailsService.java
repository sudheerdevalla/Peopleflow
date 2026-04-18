package com.hr.hrapp.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.hr.hrapp.entity.User;
import com.hr.hrapp.repository.UserRepository;

@Service
public class CustomerUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository repo;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		User user = repo.findByUsername(username);

		if (user == null) {
			throw new UsernameNotFoundException("User not found");
		}

		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
				java.util.Arrays.asList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().trim())
						)
				);
				
		//Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
	}
}