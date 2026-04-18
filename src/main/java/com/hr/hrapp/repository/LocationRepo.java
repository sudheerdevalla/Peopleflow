package com.hr.hrapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hr.hrapp.entity.Location;

public interface LocationRepo extends JpaRepository<Location, Long> {
}