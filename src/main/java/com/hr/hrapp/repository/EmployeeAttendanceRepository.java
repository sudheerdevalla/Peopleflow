package com.hr.hrapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.EmployeeAttendance;

public interface EmployeeAttendanceRepository extends JpaRepository<EmployeeAttendance, Integer> {

	EmployeeAttendance findByEmployeeId(Long employeeId);
	
}
