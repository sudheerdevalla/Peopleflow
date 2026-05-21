package com.hr.hrapp.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hr.hrapp.entity.Leave;

public interface LeaveRepository extends JpaRepository<Leave, Long> {
	
	 // 🔹 Count Sick Leaves (APPROVED only)
    int countByEmpIdAndTypeAndStatus(Long empId, String type, String status);

    // 🔹 Check if already leave exists for a date
    boolean existsByEmpIdAndDateAndStatus(Long empId, LocalDate date, String status);

    // 🔹 Get all leaves for employee
    List<Leave> findByEmpId(Long empId);

}
