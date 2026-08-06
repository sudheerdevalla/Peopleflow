package com.hr.hrapp.repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hr.hrapp.entity.Timesheet;

public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {

    // ✅ Paginated versions (NEW)
    Page<Timesheet> findByEmployeeIdAndDateBetween(
            Long employeeId,
            LocalDate start,
            LocalDate end,
            Pageable pageable
    );
    
    Page<Timesheet> findByEmployeeId(Long employeeId, Pageable pageable);
    Page<Timesheet> findByStatus(String status, Pageable pageable);

    // Keep existing non-paginated versions for backward compatibility
    List<Timesheet> findByEmployeeIdAndDateBetween(
            Long employeeId,
            LocalDate start,
            LocalDate end
    );

    List<Timesheet> findByEmployeeId(Long employeeId);
    
    List<Timesheet> findByStatus(String status);
    
    Optional<Timesheet> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
    
    boolean existsByEmployeeIdAndDate(Long employeeId, LocalDate date);
}