package com.hr.hrapp.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hr.hrapp.entity.Holiday;

@Repository
public interface HolidayRepository
        extends JpaRepository<Holiday, Long> {
	
	Holiday findByHolidayDate(LocalDate holidayDate);

}