package com.hr.hrapp.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hr.hrapp.entity.Leave;

public interface LeaveRepository extends JpaRepository<Leave, Long> {
	
	// ✅ Paginated versions (NEW)
	Page<Leave> findByEmpId(Long empId, Pageable pageable);
	Page<Leave> findByEmpIdAndStatus(Long empId, String status, Pageable pageable);
	
	// Keep existing non-paginated versions for backward compatibility
	List<Leave> findByEmpId(Long empId);
	
	// 🔹 Count Sick Leaves (APPROVED only)
    int countByEmpIdAndTypeAndStatus(Long empId, String type, String status);

    // 🔹 Check if already leave exists for a date
    boolean existsByEmpIdAndDateAndStatus(Long empId, LocalDate date, String status);
    
    long countByStatus(String status);

}
