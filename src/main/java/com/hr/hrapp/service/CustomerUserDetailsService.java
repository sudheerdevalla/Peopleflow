package com.hr.hrapp.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.hr.hrapp.entity.User;
import com.hr.hrapp.entity.Role;
import com.hr.hrapp.entity.Permission;
import com.hr.hrapp.repository.UserRepository;

@Service
public class CustomerUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository repo;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		User user = repo.findByUsername(username)
		        .orElseThrow(() ->
		                new UsernameNotFoundException("User not found"));
		Set<SimpleGrantedAuthority> authorities = new HashSet<>();
		// Add role as authority
		authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().trim()));
		// Add permissions from roles
		if (user.getRoles() != null) {
			for (Role role : user.getRoles()) {
				if (role.getPermissions() != null) {
					for (Permission perm : role.getPermissions()) {
						authorities.add(new SimpleGrantedAuthority(perm.getName()));
					}
				}
			}
		}
		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
				authorities);
	}
}