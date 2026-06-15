package com.hr.hrapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hr.hrapp.entity.Location;

public interface LocationRepo extends JpaRepository<Location, Long> {

	// Find office/location by name (case-insensitive)
	Optional<Location> findByNameIgnoreCase(String name);

}